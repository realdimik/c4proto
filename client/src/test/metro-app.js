"use strict";

import SSEConnection from "../main/sse-connection"
import Feedback      from "../main/feedback"
import VDomSender    from "../main/vdom-sender"
import VDomMix       from "../main/vdom-mix"
import MetroUi 		 from "../addon/metro-ui"
import CustomUi 	 from "../addon/custom-ui"
function fail(data){ alert(data) }

const feedback = Feedback()
const sender = VDomSender(feedback)
const metroUi = MetroUi(sender);
const customUi = CustomUi(metroUi);
const vdom = VDomMix(feedback,sender,[metroUi.transforms,customUi.transforms])
const receiversList = vdom.receiversList.concat([feedback.receivers,metroUi.receivers,customUi.receivers,{fail}])

if(parseInt(location.port)&&parseInt(location.port)!=80){
	SSEConnection(window.sseUrl||(location.protocol+"//"+location.hostname+":"+(parseInt(location.port)+1)+"/sse"), receiversList, 5)
}
else
{
	SSEConnection(window.sseUrl||(location.protocol+"//"+location.host+"/sse"), receiversList, 5)
}
