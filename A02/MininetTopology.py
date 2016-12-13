#!/usr/bin/python

from mininet.topo import Topo


def create_network(switch_data):
    topo = Topo()
    link_keys = []

    # for i in map: print i
    for switch in switch_data:
        data = switch_data[switch]
        switch = topo.addSwitch(switch, dpid=hex(data['dpid'])[2:])
        host = topo.addHost(data['host_name'], ip=data['host_ip'])
        topo.addLink(host, switch, port1=1, port2=data['dpid'])

    for start in switch_data:
        for dest in switch_data[start]['link']:
            if not (dest, start) in link_keys:
                link_keys.append((start, dest))
                topo.addLink(start, dest, port1=switch_data[dest]['dpid'], port2=switch_data[start]['dpid'])
    return topo
