package asserts.diff

import java.net.URI
import java.time.Instant
import ai.x.diff._
import asserts.domain._
import org.scalatest.{FlatSpec, Matchers}

class ATest extends FlatSpec with Matchers with DiffMatcher {

  val left: Foo = Foo(
    Bar("asdf", 5),
    List(123, 1234),
    Some(Bar("asdf", 5))
  )
  val right: Foo = Foo(
    Bar("asdf", 66),
    List(1234),
    Some(Bar("qwer", 5))
  )

  it should "work" in {
    import ai.x.diff.conversions._
    right should matchTo(right)
    left should matchTo(right)
  }

  def newEthWebhook_OUT(txHash: TxHash,
                        txFrom: AddressValue,
                        txTo: AddressValue,
                        txValue: String,
                        token: Option[EthTokenTransfer[TokenNameAddress]], // TODO
                        direction: TsDirection,
                        confirmed: Boolean): EthWebhook_OUT =
    EthWebhook_OUT(
      EthTransaction_OUT(txHash, txFrom, Some(txTo), txValue, "", 0, Instant.ofEpochSecond(0)),
      token
        .map(t =>
          EthTokenTransfer_OUT(t.token.contractAddress, t.from, t.to, t.value.toString, direction, t.token.name))
        .getOrElse(EthDirectTransfer_OUT(txFrom, txTo, txValue, direction)),
      0,
      confirmed
    )
  val wh1 = newEthWebhook_OUT(
    "asdasd",
    "azxczxc",
    "0x1213123",
    "0",
    Some(EthTokenTransfer("0x1213123", "oxKasper", 2, TokenNameAddress("ZLX", "0x123123123"))),
    TsDirection.Incoming,
    confirmed = true
  )

  val wh2 = newEthWebhook_OUT(
    "asdasd",
    "azxczxc",
    "0x1213123",
    "0-kasper",
    Some(EthTokenTransfer("0x1213123", "oxKasper", 2, TokenNameAddress("ZLX", "0x123123123"))),
    TsDirection.Outgoing,
    confirmed = false
  )

  it should "wor2k" in {
    import ai.x.diff.conversions._
    wh1 should matchTo(wh2)
  }

}
sealed trait Parent
case class Bar(s: String, i: Int) extends Parent
case class Foo(bar: Bar, b: List[Int], parent: Option[Parent]) extends Parent

case class EthWebhook_OUT(transaction: EthTransaction_OUT,
                          transfer: EthTransferData_OUT,
                          confirmations: Confirmations,
                          confirmed: Boolean)
    extends Webhook_OUT[EthWebhook_OUT] {}

trait Webhook_OUT[This <: Webhook_OUT[This]] { this: This =>

}

object HarvestTransfer {
  def isConfirmed(confirmations: Confirmations, requiredConfirmations: Confirmations): Boolean =
    requiredConfirmations <= confirmations
}

object EthWebhook_OUT {
  def apply(ew: EthWebhookToCall): EthWebhook_OUT = EthWebhook_OUT(
    EthTransaction_OUT(ew.transaction),
    EthTransferData_OUT(ew.transfer, ew.harvestedAddress),
    ew.confirmations,
    ew.confirmed
  )
}

case class EthNodeTransaction(txHash: TxHash,
                              from: AddressValue,
                              to: Option[AddressValue],
                              value: BigDecimal,
                              blockHash: BlockHash,
                              blockHeight: BlockNumber,
                              blockTimestamp: Instant,
                              input: String)
    extends NodeTransaction {
  def isContractCall: Boolean = input != "0x0"
}

trait NodeTransaction {
  def txHash: TxHash
}

case class EthTransaction(hash: TxHash,
                          from: AddressValue,
                          to: Option[AddressValue],
                          value: BigDecimal,
                          blockHash: BlockHash,
                          blockHeight: BlockNumber,
                          blockTimestamp: Instant)

object EthTransaction {
  def apply(nodeTx: EthNodeTransaction): EthTransaction =
    EthTransaction(nodeTx.txHash,
                   nodeTx.from,
                   nodeTx.to,
                   nodeTx.value,
                   nodeTx.blockHash,
                   nodeTx.blockHeight,
                   nodeTx.blockTimestamp)
}

case class EthWebhookToCall(transferId: Id,
                            harvestedAddress: AddressValue,
                            webhook: URI,
                            transaction: EthTransaction,
                            transfer: EthTransferData[TokenNameAddress],
                            confirmations: Confirmations,
                            requiredConfirmations: Confirmations)
    extends WebhookToCall {

  override def toOut: EthWebhook_OUT = EthWebhook_OUT(this)
}

trait WebhookToCall {
  def transferId: Id
  def confirmations: Confirmations
  def requiredConfirmations: Confirmations
  def webhook: URI

  def confirmed: Boolean = HarvestTransfer.isConfirmed(confirmations, requiredConfirmations)

  def toOut: Webhook_OUT[_]
}

case class EthTransaction_OUT(hash: TxHash,
                              from: AddressValue,
                              to: Option[AddressValue],
                              value: String,
                              blockHash: BlockHash,
                              blockHeight: BlockNumber,
                              blockTimestamp: Instant)

object EthTransaction_OUT {
  def apply(tx: EthTransaction): EthTransaction_OUT =
    EthTransaction_OUT(tx.hash, tx.from, tx.to, tx.value.toString, tx.blockHash, tx.blockHeight, tx.blockTimestamp)
}

case class EthTokenTransfer[Token](from: AddressValue, to: AddressValue, value: BigDecimal, token: Token)
    extends EthTransferData[Token]

sealed trait EthTransferData[+Token] {
  def from: AddressValue
  def value: BigDecimal
}

sealed trait EthTransferData_OUT {
  def from: AddressValue
  def value: String
}

case class TokenNameAddress(name: String, contractAddress: AddressValue)

object EthTransferData_OUT {
  def apply(td: EthTransferData[TokenNameAddress], harvestedAddress: AddressValue): EthTransferData_OUT = {
    def direction(from: AddressValue, to: Option[AddressValue]) =
      if (harvestedAddress == from) TsDirection.Outgoing
      else if (to.contains(harvestedAddress)) TsDirection.Incoming
      else
        throw new RuntimeException(s"Cannot determine transfer direction for $td, harvested address: $harvestedAddress")

    td match {
      case EthDirectTransfer(from, to, value) =>
        EthDirectTransfer_OUT(from, to, value.toString, direction(from, Some(to)))
      case EthContractCall(from, to, value) => EthContractCall_OUT(from, to, value.toString, direction(from, to))
      case EthTokenTransfer(from, to, value, token) =>
        EthTokenTransfer_OUT(token.contractAddress, from, to, value.toString, direction(from, Some(to)), token.name)
      case EthForwardTransfer(contractAddress, from, to, value) =>
        EthForwardTransfer_OUT(contractAddress, from, to, value.toString, direction(from, Some(to)))
    }
  }
}

case class EthDirectTransfer_OUT(from: AddressValue, to: AddressValue, value: String, direction: TsDirection)
    extends EthTransferData_OUT

case class EthContractCall_OUT(from: AddressValue, to: Option[AddressValue], value: String, direction: TsDirection)
    extends EthTransferData_OUT

case class EthTokenTransfer_OUT(contractAddress: AddressValue,
                                from: AddressValue,
                                to: AddressValue,
                                value: String,
                                direction: TsDirection,
                                tokenName: String)
    extends EthTransferData_OUT

case class EthForwardTransfer_OUT(contractAddress: AddressValue,
                                  from: AddressValue,
                                  to: AddressValue,
                                  value: String,
                                  direction: TsDirection)
    extends EthTransferData_OUT

case class EthDirectTransfer(from: AddressValue, to: AddressValue, value: BigDecimal) extends EthTransferData[Nothing]

case class EthContractCall(from: AddressValue, to: Option[AddressValue], value: BigDecimal)
    extends EthTransferData[Nothing]

case class EthForwardTransfer(contractAddress: AddressValue, from: AddressValue, to: AddressValue, value: BigDecimal)
    extends EthTransferData[Nothing]

sealed trait TsDirection // ts - transfer
object TsDirection {
  case object Incoming extends TsDirection
  case object Outgoing extends TsDirection
}
