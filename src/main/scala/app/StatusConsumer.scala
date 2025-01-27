package app

trait StatusConsumer {
  def consumeStatus(link: String, status: LocalCheckedStatus | RemoteCheckedStatus): Unit
}
