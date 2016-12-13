import pickle

from ryu.base import app_manager
from ryu.controller import ofp_event
from ryu.controller.handler import CONFIG_DISPATCHER
from ryu.controller.handler import set_ev_cls
from ryu.lib.packet.ether_types import ETH_TYPE_IP
from ryu.ofproto import ofproto_v1_3


class L2Switch(app_manager.RyuApp):
    OFP_VERSIONS = [ofproto_v1_3.OFP_VERSION]
    path = "AbileneTM-all/topo-2003-04-10.txt"

    def __init__(self, *args, **kwargs):
        super(L2Switch, self).__init__(*args, **kwargs)
        config_file_path = 'config'
        self.switch_data = pickle.load(open(config_file_path, 'r'))

    @set_ev_cls(ofp_event.EventOFPSwitchFeatures, CONFIG_DISPATCHER)
    def switch_features_handler(self, ev):
        msg = ev.msg
        print(msg)

    def add_flow(self, datapath, priority, match, actions, buffer_id=None):
        ofproto = datapath.ofproto
        parser = datapath.ofproto_parser

        inst = [parser.OFPInstructionActions(ofproto.OFPIT_APPLY_ACTIONS,
                                             actions)]
        if buffer_id:
            mod = parser.OFPFlowMod(datapath=datapath, buffer_id=buffer_id,
                                    priority=priority, match=match,
                                    instructions=inst)
        else:
            mod = parser.OFPFlowMod(datapath=datapath, priority=priority,
                                    match=match, instructions=inst)
        datapath.send_msg(mod)

    @set_ev_cls(ofp_event.EventOFPSwitchFeatures, CONFIG_DISPATCHER)
    def switch_features_handler(self, ev):
        datapath = ev.msg.datapath
        parser = datapath.ofproto_parser
        dpid = datapath.id
        for s in self.switch_data:
            if self.switch_data[s]['dpid'] == dpid:
                switch = self.switch_data[s]
                break

        self.logger.info('switch=%s dpid=%s', s, dpid)

        for hop in switch['next_hop']:
            dst_ip = hop
            out_port = switch['next_hop'][hop]
            match = parser.OFPMatch(ipv4_dst=dst_ip, eth_type=ETH_TYPE_IP)
            actions = [parser.OFPActionOutput(out_port)]
            self.add_flow(datapath, 0, match, actions)
        dst_ip = switch['host_ip']
        out_port = dpid
        match = parser.OFPMatch(ipv4_dst=dst_ip, eth_type=ETH_TYPE_IP)
        actions = [parser.OFPActionOutput(out_port)]
        self.add_flow(datapath, 0, match, actions)
