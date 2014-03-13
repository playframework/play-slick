package scala.slick.jdbc

/** Reimplementation of the transaction semantics of Slick's BaseSession#withTransaction,
  * but in an API which can be driven from multiple asynchronous callbacks rather than only a
  * single block.
  *
  * NOTE: Used by SlickPlayIteratees to provide read consistency across chunked reads.
  */
class SessionWithAsyncTransaction(db: JdbcBackend#Database) extends JdbcBackend.BaseSession(db.asInstanceOf[JdbcBackend.Database]) {
  private var hasTransactionFailed = false // don't allow asynchronously re-opening after a failed transaction

  /** Execute the given block with an active async transaction
    *
    * NOTE: If the given block throws an exception, then:
    *   1. The open transaction will be rolled back
    *   2. Further calls are not allowed, throwing IllegalStateException, create a new Session instead
    */
  def withAsyncTransaction[T](f: JdbcBackend#Session => T): T = {
    ensureAsyncTransactionIsStarted()

    try {
      f(this)
    } catch {
      case ex: Throwable => {
        doRollback = true
        hasTransactionFailed = true
        ensureAsyncTransactionIsCompleted()
        throw ex
      }
    }
  }

  /** Bring any active transaction to completion, resulting in either commit or rollback */
  def ensureAsyncTransactionIsCompleted(): Unit = {
    if (inTransaction) { completeAsyncTransaction() }
  }

  def ensureAsyncTransactionIsStarted(): Unit = {
    if (!inTransaction) { startAsyncTransaction() }
  }

  def startAsyncTransaction(): Unit = {
    if (hasTransactionFailed) { throw new IllegalStateException("An async transaction was already rolled back on this session. A new session is required to create a new transaction") }
    if (inTransaction) { throw new IllegalStateException("An async transaction was already in progress") }

    conn.setAutoCommit(false)
    inTransaction = true
    doRollback = false
  }

  def completeAsyncTransaction(): Unit = {
    if (!inTransaction) { throw new IllegalStateException("No async transaction was in progress") }

    try {
      if (doRollback) { conn.rollback() } else { conn.commit() }
    } finally {
      conn.setAutoCommit(true)
      inTransaction = false
      close()
      open = false
    }
  }

  def isOpen: Boolean = {
    conn // force instantiation of lazy val so we can assume it starts open
    open
  }

}