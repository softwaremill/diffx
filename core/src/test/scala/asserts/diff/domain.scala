package asserts

import java.util.UUID

package object domain {
  type Id = UUID
  type WalletId = Int
  type PublicKey = String
  type PrivateKey = String
  type BlockHash = String
  type TxHash = String
  type RawTx = String
  type AddressValue = String
  type BlockNumber = Long
  type Confirmations = Long
}
