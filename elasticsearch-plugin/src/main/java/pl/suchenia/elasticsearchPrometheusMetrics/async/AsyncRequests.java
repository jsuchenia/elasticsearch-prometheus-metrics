package pl.suchenia.elasticsearchPrometheusMetrics.async;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.node.stats.NodeStats;
import org.elasticsearch.action.admin.cluster.node.stats.NodesStatsRequest;
import org.elasticsearch.action.admin.cluster.node.stats.NodesStatsResponse;
import org.elasticsearch.action.admin.cluster.node.usage.NodeUsage;
import org.elasticsearch.action.admin.cluster.node.usage.NodesUsageRequest;
import org.elasticsearch.action.admin.cluster.node.usage.NodesUsageResponse;
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequest;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.cluster.tasks.PendingClusterTasksRequest;
import org.elasticsearch.action.admin.cluster.tasks.PendingClusterTasksResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterState;

import java.util.concurrent.CompletableFuture;

public class AsyncRequests {
    public static CompletableFuture<NodeStats> getNodeStats(final Client client) {
        ActionListenerAdapter<NodesStatsResponse> result = new ActionListenerAdapter<>();

        NodesStatsRequest nodesStatsRequest = new NodesStatsRequest("_local").all();
        client.admin().cluster().nodesStats(nodesStatsRequest, result.asActionListener());
        return result.thenApply((response)-> response.getNodes().get(0));
    }

    public static CompletableFuture<ClusterHealthResponse> getClusterHealth(final Client client) {
        ActionListenerAdapter<ClusterHealthResponse> result = new ActionListenerAdapter<>();
        ClusterHealthRequest clusterHealthRequest = new ClusterHealthRequest();
        client.admin().cluster().health(clusterHealthRequest, result.asActionListener());
        return result;
    }

    public static CompletableFuture<ClusterState> getClusterState(final Client client) {
        ActionListenerAdapter<ClusterStateResponse> result = new ActionListenerAdapter<>();
        ClusterStateRequest request = new ClusterStateRequest().routingTable(false).nodes(false);

        client.admin().cluster().state(request, result.asActionListener());
        return result.thenApply(ClusterStateResponse::getState);
    }

    public static CompletableFuture<PendingClusterTasksResponse> getPendingTasks(final Client client) {
        ActionListenerAdapter<PendingClusterTasksResponse> result = new ActionListenerAdapter<>();
        PendingClusterTasksRequest request = new PendingClusterTasksRequest();

        client.admin().cluster().pendingClusterTasks(request, result.asActionListener());
        return result;
    }

    public static CompletableFuture<NodeUsage> getNodesUsage(final Client client) {
        ActionListenerAdapter<NodesUsageResponse> result = new ActionListenerAdapter<>();
        NodesUsageRequest request = new NodesUsageRequest("_local").all();

        client.admin().cluster().nodesUsage(request, result.asActionListener());

        return result.thenApply((response) -> response.getNodes().get(0));
    }
}
