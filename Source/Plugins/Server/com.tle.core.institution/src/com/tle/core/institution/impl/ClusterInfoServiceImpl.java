package com.tle.core.institution.impl;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.utils.ZKPaths;

import com.dytech.common.net.NetworkUtils;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Lists;
import com.tle.common.Pair;
import com.tle.core.application.StartupBean;
import com.tle.core.guice.Bind;
import com.tle.core.institution.ClusterInfoService;
import com.tle.core.zookeeper.ZookeeperService;

@SuppressWarnings("nls")
@Bind(ClusterInfoService.class)
@Singleton
public class ClusterInfoServiceImpl implements ClusterInfoService, StartupBean
{
	@Inject
	private ZookeeperService zookeeperService;
	private PathChildrenCache ipCache;

	@Override
	public void startup()
	{
		if( !zookeeperService.hasStarted() )
		{
			throw new RuntimeException("Dependent ZK service not started!");
		}
		if( zookeeperService.isCluster() )
		{
			String allIps = Joiner.on(", ").join(
				Lists.transform(NetworkUtils.getInetAddresses(),
					new Function<Pair<NetworkInterface, InetAddress>, String>()
					{
						@Override
						public String apply(Pair<NetworkInterface, InetAddress> ni)
						{
							return ni.getFirst().getName() + "=" + ni.getSecond().getHostAddress();
						}
					}));
			zookeeperService.createNode(IP_LIST_ZKPATH, allIps);
			ipCache = zookeeperService.createPathCache(IP_LIST_ZKPATH, true);
		}
	}

	@Override
	public Map<String, String> getIpAddresses()
	{
		List<ChildData> nodes = ipCache.getCurrentData();
		Builder<String, String> b = ImmutableMap.builder();
		for( ChildData childData : nodes )
		{
			b.put(ZKPaths.getNodeFromPath(childData.getPath()), new String(childData.getData()));
		}
		return b.build();
	}
}
