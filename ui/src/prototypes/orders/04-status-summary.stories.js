import React from 'react'
import ProgressInfo from '../../components/ProgressInfo'

export default {
  title: 'Prototypes/Orders/Status Summary'
}

const statusLabels = {
  IN_APPROVAL: 'Genehmigung',
  TO_PICKUP: 'Abholung',
  TO_RETURN: 'Rückgabe',
  RETURNED: 'Alle Gegenstände zurückgebracht',
  REJECTED: 'Bestellung wurde abgelehnt',
  EXPIRED_UNAPPROVED: 'Abgelaufen (nicht genehmigt)',
  CANCELED: 'Bestellung wurde storniert',
  EXPIRED: 'Abgelaufen (nicht abgeholt)',
  OVERDUE: 'Rückgabe überfällig'
}

function Example({ children }) {
  return <div className="border rounded py-3 px-2 mb-4 d-flex flex-column gap-2">{children}</div>
}

export function structure() {
  return (
    <>
      <h1>`My Rentals` Status Summary</h1>
      <h2>Structure</h2>

      <h3>1</h3>
      <p className="text-muted">
        The status summary consists of up to three <b>status rows</b>, one for each sub process.
      </p>
      <p className="text-muted">
        The sub processes are: <b>approval, pickup, return.</b>
      </p>
      <p className="text-muted">Example where all three status rows are shown:</p>
      <Example>
        <ProgressInfo
          title={statusLabels.IN_APPROVAL}
          info="4 von 5 Gegenständen genehmigt"
          totalCount={5}
          doneCount={4}
        />
        <ProgressInfo
          title={statusLabels.TO_PICKUP}
          info="3 von 5 Gegenständen abgeholt"
          totalCount={5}
          doneCount={3}
        />
        <ProgressInfo
          title={statusLabels.TO_RETURN}
          info="2 von 5 Gegenständen zurückgebracht"
          totalCount={5}
          doneCount={2}
        />
      </Example>
      <p className="text-muted">
        The displayed quantities relate to the number of indididual items (not to the number of distinct models).
      </p>
      <p className="text-muted">
        The displayed total quantity can be modified in the lending process (e.g. when &quot;options&quot; like cables
        are added by the lending person).
      </p>

      <h3>2</h3>
      <p className="text-muted">
        Status lines are shown for sub processes being <b>in progress</b> only.
      </p>
      <p className="text-muted">
        In the following example, only the status row for the <b>pickup</b> process is shown.
      </p>
      <p className="text-muted">
        The <b>approval</b> process is not in progress anymore.
      </p>
      <p className="text-muted">
        The <b>return</b> process is not in progress yet.
      </p>
      <Example>
        <ProgressInfo
          title={statusLabels.TO_PICKUP}
          info="0 von 5 Gegenständen abgeholt"
          totalCount={5}
          doneCount={0}
        />
      </Example>

      <h3>3</h3>
      <p className="text-muted">
        When a rental ends by all its items reaching a coherent terminal state (one of the following four), the plain
        title for the relevant status is displayed:
      </p>
      <p className="text-muted">Approval - canceled by customer:</p>
      <Example>
        <ProgressInfo title={statusLabels.CANCELED} />
      </Example>
      <p className="text-muted">Approval - all items rejected:</p>
      <Example>
        <ProgressInfo title={statusLabels.REJECTED} />
      </Example>
      <p className="text-muted">Approval - not approved in time:</p>
      <Example>
        <ProgressInfo title={statusLabels.EXPIRED_UNAPPROVED} />
      </Example>
      <p className="text-muted">Pick up - customer missed all items:</p>
      <Example>
        <ProgressInfo title={statusLabels.EXPIRED} />
      </Example>
      <p className="text-muted">Return - all items returned:</p>
      <Example>
        <ProgressInfo title={statusLabels.RETURNED} />
      </Example>

      <h3>4</h3>
      <p className="text-muted">
        &quot;Drop-out items&quot; - when a sub process did reduce the number of items for the next sub process, there
        is a info in parantheses with quantity and reason.
      </p>
      <p className="text-muted">In this example 2 of 5 items were rejected, which means only 3 items are to pick up:</p>
      <Example>
        <ProgressInfo
          title={statusLabels.IN_APPROVAL}
          info="2 von 5 Gegenständen genehmigt (2 abgelehnt)"
          totalCount={5}
          doneCount={4}
        />
        <ProgressInfo
          title={statusLabels.TO_PICKUP}
          info="0 von 3 Gegenständen abgeholt"
          totalCount={3}
          doneCount={0}
        />
      </Example>
      <p className="text-muted">
        In the case of &quot;drop-out items&quot; the status row is shown even when the sub process is completed.
        However the visual progress bar is omitted.
      </p>
      <Example>
        <ProgressInfo title={statusLabels.IN_APPROVAL} info="3 von 5 Gegenständen genehmigt (2 abgelehnt)" />
        <ProgressInfo
          title={statusLabels.TO_PICKUP}
          info="0 von 3 Gegenständen abgeholt"
          totalCount={3}
          doneCount={0}
        />
      </Example>

      <div>&nbsp;</div>
      <div>&nbsp;</div>
    </>
  )
}

export function typicalFlow() {
  return (
    <>
      <h1>`My Rentals` Status Summary</h1>
      <h2>Typical Flow</h2>
      <p className="text-muted">
        Customer has ordered and is waiting for approval.{' '}
        <span className="small">
          Only the approval status row is shown because pickup and return sub process have not been started yet.
        </span>
      </p>
      <Example>
        <ProgressInfo
          title={statusLabels.IN_APPROVAL}
          info="0 von 5 Gegenständen genehmigt"
          totalCount={5}
          doneCount={0}
        />
      </Example>
      <p className="text-muted">
        Lending manager has approved the order.{' '}
        <span className="small">
          The pickup sub process has started. The status row for approval is now hidden because the sub process is
          complete.
        </span>
      </p>
      <Example>
        <ProgressInfo
          title={statusLabels.TO_PICKUP}
          info="0 von 5 Gegenständen abgeholt"
          totalCount={5}
          doneCount={0}
        />
      </Example>
      <p className="text-muted">
        Customer did pick up the first item.{' '}
        <span className="small">The return sub process has started, the pickup sub process is ongoing.</span>
      </p>
      <Example>
        <ProgressInfo
          title={statusLabels.TO_PICKUP}
          info="1 von 5 Gegenständen abgeholt"
          totalCount={5}
          doneCount={1}
        />
        <ProgressInfo
          title={statusLabels.TO_RETURN}
          info="0 von 5 Gegenständen zurückgebracht"
          totalCount={5}
          doneCount={0}
        />
      </Example>
      <p className="text-muted">
        Customer picked more items and returned one. <span className="small">Both sub processes ongoing.</span>
      </p>
      <Example>
        <ProgressInfo
          title={statusLabels.TO_PICKUP}
          info="3 von 5 Gegenständen abgeholt"
          totalCount={5}
          doneCount={3}
        />
        <ProgressInfo
          title={statusLabels.TO_RETURN}
          info="1 von 5 Gegenständen zurückgebracht"
          totalCount={5}
          doneCount={1}
        />
      </Example>
      <p className="text-muted">
        Customer picked the last items.{' '}
        <span className="small">Pick-up sub process is complete and therefore hidden now.</span>
      </p>
      <Example>
        <ProgressInfo
          title={statusLabels.TO_RETURN}
          info="1 von 5 Gegenständen zurückgebracht"
          totalCount={5}
          doneCount={1}
        />
      </Example>
      <p className="text-muted">
        Customer returned all items.{' '}
        <span className="small">The rental has ended and the terminal status is shown (plain title only)</span>
      </p>
      <Example>
        <ProgressInfo title={statusLabels.RETURNED} />
      </Example>
      <div>&nbsp;</div>
      <div>&nbsp;</div>
    </>
  )
}

export function detailsApproval() {
  return (
    <>
      <h1>`My Rentals` Status Summary</h1>
      <h2>Details: Approval</h2>
      <p className="text-muted">Cancelled by customer before approval</p>
      <Example>
        <ProgressInfo title={statusLabels.CANCELED} />
      </Example>
      <p className="text-muted">Rejected by lending manager</p>
      <Example>
        <ProgressInfo title={statusLabels.REJECTED} />
      </Example>
      <p className="text-muted">Partially approved</p>
      <p className="text-muted">
        Partial approval is possible when the order contains items from more than one inventory pool.
      </p>
      <Example>
        <ProgressInfo
          title={statusLabels.IN_APPROVAL}
          info="3 von 5 Gegenständen genehmigt"
          totalCount={5}
          doneCount={3}
        />
        <ProgressInfo
          title={statusLabels.TO_PICKUP}
          info="0 von 5 Gegenständen abgeholt"
          totalCount={5}
          doneCount={0}
        />
      </Example>
      <p className="text-muted">Partially rejected</p>
      <p className="text-muted">
        In the following example, pool A (2 items) approved, pool B (2 items) rejected, and pool C (1 item) has not
        approved yet:
      </p>
      <p className="text-muted">(Note: the total in the pick up progress bar is 3 = 5 ordered minus 2 rejected)</p>
      <Example>
        <ProgressInfo
          title={statusLabels.IN_APPROVAL}
          info="2 von 5 Gegenständen genehmigt (2 abgelehnt)"
          totalCount={5}
          doneCount={4}
        />
        <ProgressInfo
          title={statusLabels.TO_PICKUP}
          info="0 von 3 Gegenständen abgeholt"
          totalCount={3}
          doneCount={0}
        />
      </Example>
      <p className="text-muted">The same example after pool C approved:</p>
      <p className="text-muted">
        (Note: the approval summary is still shown because its affects the next process by reducing the total number of
        items to pick up)
      </p>
      <Example>
        <ProgressInfo title={statusLabels.IN_APPROVAL} info="3 von 5 Gegenständen genehmigt (2 abgelehnt)" />
        <ProgressInfo
          title={statusLabels.TO_PICKUP}
          info="0 von 3 Gegenständen abgeholt"
          totalCount={3}
          doneCount={0}
        />
      </Example>
      <p className="text-muted">Expiration</p>
      <p className="text-muted">
        Reservation items have the temporal pseudo-status <b>`expired (not approved)`</b> when they are are neither
        rejected nor approved until the reservation end date.
      </p>
      <p className="text-muted">All items are expired:</p>
      <Example>
        <ProgressInfo title={statusLabels.EXPIRED_UNAPPROVED} />
      </Example>
      <p className="text-muted">Some expired, some still to be approved:</p>
      <Example>
        <ProgressInfo
          title={statusLabels.IN_APPROVAL}
          info="0 von 5 Gegenständen genehmigt (2 abgelaufen)"
          totalCount={5}
          doneCount={2}
        />
      </Example>{' '}
      <div>&nbsp;</div>
      <div>&nbsp;</div>
    </>
  )
}

export function detailsPickup() {
  return (
    <>
      <h1>`My Rentals` Status Summary</h1>
      <h2>Details: Pickup</h2>
      <p className="text-muted">Expiration</p>
      <p className="text-muted">
        Reservation items have the temporal pseudo-status <b>`expired (not picked up)`</b> when they are not picked up
        until the reservation end date.
      </p>
      <p className="text-muted">All items are expired:</p>
      <Example>
        <ProgressInfo title={statusLabels.EXPIRED} />
      </Example>
      <p className="text-muted">Some are expired, and the others are not picked up yet:</p>
      <Example>
        <ProgressInfo
          title={statusLabels.TO_PICKUP}
          info="0 von 5 Gegenständen abgeholt (2 abgelaufen)"
          totalCount={5}
          doneCount={2}
        />
      </Example>
      <p className="text-muted">Then some of the others have been picked up:</p>
      <Example>
        <ProgressInfo
          title={statusLabels.TO_PICKUP}
          info="2 von 5 Gegenständen abgeholt (2 abgelaufen)"
          totalCount={5}
          doneCount={4}
        />
        <ProgressInfo
          title={statusLabels.TO_RETURN}
          info="0 von 3 Gegenständen zurückgebracht"
          totalCount={3}
          doneCount={0}
        />
      </Example>
      <p className="text-muted">Then all others have been picked up:</p>
      <p className="text-muted">
        (Note how the pickup summary is still shown because its affects the next process by reducing total number of
        items to return)
      </p>
      <ul className="list-group mb-4">
        <Example>
          <ProgressInfo title={statusLabels.TO_PICKUP} info="3 von 5 Gegenständen abgeholt (2 abgelaufen)" />
          <ProgressInfo
            title={statusLabels.TO_RETURN}
            info="0 von 3 Gegenständen zurückgebracht"
            totalCount={3}
            doneCount={0}
          />
        </Example>
      </ul>
      <div>&nbsp;</div>
      <div>&nbsp;</div>
    </>
  )
}

export function detailsReturn() {
  return (
    <>
      <h1>`My Rentals` Status Summary</h1>
      <h2>Details: Return</h2>
      <p className="text-muted">Overdue</p>
      <p className="text-muted">Items are overdue when they are not returned until the reservation end date.</p>
      <p className="text-muted">
        When at least one item of a rental is overdue, the status title changes and is marked as warning. The number of
        overdue items is added to the normal progress info text.
      </p>
      <Example>
        <ProgressInfo
          title={<div className="invalid-feedback-icon text-danger">{statusLabels.OVERDUE}</div>}
          info="0 von 5 Gegenständen zurückgebracht (5 überfällig)"
          totalCount={5}
          doneCount={0}
        />
      </Example>
      <p className="text-muted">Example when not all items are overdue, and some are returned already:</p>
      <Example>
        <ProgressInfo
          title={<div className="invalid-feedback-icon text-danger">{statusLabels.OVERDUE}</div>}
          info="2 von 5 Gegenständen zurückgebracht (2 überfällig)"
          totalCount={5}
          doneCount={2}
        />
      </Example>

      <div>&nbsp;</div>
      <div>&nbsp;</div>
    </>
  )
}
