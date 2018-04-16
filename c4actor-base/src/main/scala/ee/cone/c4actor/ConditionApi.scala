package ee.cone.c4actor

trait Condition[Model] extends Product {
  def check(model: Model): Boolean
}

trait SerializableCondition[Model] extends Condition[Model] {
  def getPK(modelCl: Class[Model]):QAdapterRegistry ⇒ String
}

trait ConditionFactoryApp {
  def conditionFactory: ModelConditionFactory[_]
}

trait ModelConditionFactory[Model] {
  def of[OtherModel<:Product]: ModelConditionFactory[OtherModel]
  def ofWithCl[OtherModel <: Product]: Class[OtherModel] ⇒ ModelConditionFactory[OtherModel]
  def intersect: (Condition[Model],Condition[Model]) ⇒ Condition[Model]
  def union: (Condition[Model],Condition[Model]) ⇒ Condition[Model]
  def any: Condition[Model]
  def leaf[By<:Product,Field](lens: ProdLens[Model,Field], by: By, byOptions: List[MetaAttr])(
    implicit check: ConditionCheck[By,Field]
  ): Condition[Model]
  def filterMetaList[Field]: ProdLens[Model,Field] ⇒ List[MetaAttr]
}
trait ConditionCheck[By<:Product,Field] extends Product {
  def prepare: List[MetaAttr] ⇒ By ⇒ By
  def check: By ⇒ Field ⇒ Boolean
}
trait ProdCondition[By<:Product,Model] extends SerializableCondition[Model] {
  def by: By
  def metaList: List[MetaAttr]
}
