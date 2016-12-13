# coding=utf-8
# !/usr/bin/python
import pickle
import plotly as py
import plotly.graph_objs as go
from collections import defaultdict
from functools import partial
from time import sleep
from mininet.node import Ryu, OVSSwitch

from Dijkstras import Graph
from MininetTopology import create_network

from contextlib import contextmanager

from mininet.net import Mininet

path = "AbileneTM-all/topo-2003-04-10.txt"


# 4. Select one host and run ping to all other hosts. Record the latencies.
# 5. Compute a VLB routing strategy. For each source-destination pair, first route traffic from the source
# to a random switch, and then from that switch to the destination.
# 6. Use the Ryu controller to configure the switches with the VLB forwarding rules.
# 7. Select the same host as in step 4 and run ping to all other hosts.
# 8. Because VLB is randomized, repeat steps 5–7 several times with different choices of random intermediate nodes. Record the average of the latencies across all runs.
# 9. Produce a bar chart, comparing the shortest path latency to the VLB latency for each destination.


def read_topology():
    link = False
    g = Graph()
    with open(path) as f:
        for line in f:
            data = line.split()
            if line.startswith("#"):
                continue
            if line.strip() == "link":
                link = True
            elif link:
                g.add_edge(data[0], data[1], int(data[3]))
    return g


def switch_data_generator(graph):
    switch_data = defaultdict(dict)
    id_counter = 1

    for vertex in graph.vertices:
        switch_data[vertex]['dpid'] = id_counter
        switch_data[vertex]['host_ip'] = '10.0.0.' + str(id_counter)
        switch_data[vertex]['host_name'] = 'h_' + vertex
        switch_data[vertex]['link'] = []
        switch_data[vertex]['next_hop'] = {}
        id_counter += 1

    # add links
    for vertex in graph.vertices:
        for neighbor in graph.vertices[vertex]:
            switch_data[vertex]['link'].append(neighbor)

    # nextHops
    for src in graph.vertices:
        for dst in graph.vertices:
            if src != dst:
                next_hop = graph.shortest_path(src, dst)[0]
                next_dpid = switch_data[next_hop]['dpid']
                dst_ip = switch_data[dst]['host_ip']
                switch_data[src]['next_hop'][dst_ip] = next_dpid
    return switch_data


def main():
    g = read_topology()
    switch_data = switch_data_generator(g)
    topo = create_network(switch_data)

    mininet_controller = Ryu('ruy_controller', 'RuyController.py')
    switch = partial(OVSSwitch, protocols='OpenFlow13')
    net = Mininet(topo=topo, switch=switch, build=True, controller=mininet_controller)
    net.staticArp()
    config_file_path = 'config'
    pickle.dump(switch_data, open(config_file_path, 'w'))
    net.start()
    sleep(1.5)
    host = net.hosts[0]
    latency = defaultdict(dict)
    x=[]
    y=[]
    for dst in net.hosts:
        if dst != host:
            s = host.cmd('ping -c 1 %s' % dst.IP())
            time = s.split()[13].split('=')[1]
            start = host.name[2:]
            end = dst.name[2:]
            x.append(end)
            y.append(time)
            latency[start][end] = time
            print (start + ' -> ' + end + ' time : ' + time + ' ms')
    net.stop()

    data = [go.Bar(
        x=x,
        y=y
    )]

    py.offline.plot(data, filename='basic-bar.html')


@contextmanager
def mininet_network(*args, **kwargs):
    net = None
    try:
        net = Mininet(*args, **kwargs)
        yield net
    finally:
        if net is not None:
            net.stop()


if __name__ == '__main__':
    main()
