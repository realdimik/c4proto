package ee.cone.c4gate.dep.request

import ee.cone.c4actor.Types.SrcId
import ee.cone.c4actor.dep.DepTypeContainer.ContextId
import ee.cone.c4actor.{AssemblesApp, ProtocolsApp, QAdapterRegistry, WithPK}
import ee.cone.c4actor.dep._
import ee.cone.c4assemble.Types.Values
import ee.cone.c4assemble.{Assemble, assemble}
import ee.cone.c4gate.AlienProtocol.FromAlienState
import ee.cone.c4gate.dep.request.DepFilteredListRequestProtocol.FilteredListRequest
import ee.cone.c4proto.{Id, Protocol, protocol}

case class FLRequestDef(listName: String, requestDep: Dep[_])

trait FilterListRequestApi {
  def filterDepList: List[FLRequestDef] = Nil
}

trait FilterListRequestHandlerApp extends RequestHandlersApp with AssemblesApp with ProtocolsApp with FilterListRequestApi {

  override def handlers: List[RequestHandler[_]] = filterDepList.map(df ⇒ FilteredListRequestHandler(df.requestDep, df.listName)) ::: super.handlers

  override def assembles: List[Assemble] = filterDepList.map(df ⇒ new FilterListRequestCreator(qAdapterRegistry, df.listName)) ::: super.assembles

  override def protocols: List[Protocol] = DepFilteredListRequestProtocol :: super.protocols

  def qAdapterRegistry: QAdapterRegistry
}

case class FilteredListRequestHandler(fListDep: Dep[_], listName: String) extends RequestHandler[FilteredListRequest] {
  override def canHandle: Class[FilteredListRequest] = classOf[FilteredListRequest]

  override def handle: FilteredListRequest => (Dep[_], ContextId) = request ⇒ (fListDep, request.contextId)
}

case class FilteredListResponse(srcId: String, listName: String, response: Option[_], sessionKey: String)

@assemble class FilterListRequestCreator(val qAdapterRegistry: QAdapterRegistry, listName: String) extends Assemble with DepAssembleUtilityImpl {

  def SparkFilterListRequest(
    key: SrcId,
    alienTasks: Values[FromAlienState]
  ): Values[(SrcId, DepOuterRequest)] =
    for {
      alienTask ← alienTasks
    } yield {
      val filterRequest = FilteredListRequest(alienTask.sessionKey, listName)
      WithPK(generateDepOuterRequest(filterRequest, alienTask.sessionKey))
    }

  def FilterListResponseGrabber(
    key: SrcId,
    responses: Values[DepOuterResponse]
  ): Values[(SrcId, FilteredListResponse)] =
    for {
      resp ← responses
      if resp.request.innerRequest.request.isInstanceOf[FilteredListRequest] && resp.request.innerRequest.request.asInstanceOf[FilteredListRequest].listName == listName
    } yield {
      WithPK(FilteredListResponse(resp.request.srcId, listName, resp.value, resp.request.innerRequest.request.asInstanceOf[FilteredListRequest].contextId))
    }
}

@protocol object DepFilteredListRequestProtocol extends Protocol {

  @Id(0x0a01) case class FilteredListRequest(
    @Id(0x0a02) contextId: String,
    @Id(0x0a03) listName: String
  )

}