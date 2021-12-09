# cart ux

## invalid reservations

NOTE: "calendar decoration" needs to be improved in general (e.g. colors for showing closed days before selecting them),
this is not mentioned in each example.

TODO:

- case for `models.maintenance_period` (likely not used at ZHdK, maybe ignore for now)
- next: also check the order panel (on model detail page) for all those cases. now we check what happens when i already have a reservation, but we should also check that i can not create one.

### Reservation Advance Days

- error message: PENDING
- order panel: OK
- calendar decoration: ?

### Invalid Start Date

- error message: OK
- order panel: OK
- calendar decoration: OK

### No Access To Pool

- error message: PENDING
- order panel: PENDING
- calendar decoration: PENDING

### Holiday on End Date

- error message: OK
- order panel: OK
- calendar decoration: TO BE DEFINED

### Max Visits Count Pick Up

- possible API bug!

- error message: PENDING
- order panel: PENDING
- calendar decoration: PENDING

### Max Visits Count Return

- possible API bug!

- error message: PENDING
- order panel: PENDING
- calendar decoration: PENDING

### Maximum Reservation Time

- error message: PENDING
- order panel: PENDING
- calendar decoration: PENDING

### Quantity To High

- error message: OK
- order panel: OK
- calendar decoration: OK

### Model With No Items

- error message: PENDING
- order panel: PENDING
- calendar decoration: NONE

### OK and Timed Out

- all OK, example is **a valid reservation**

### OK and Not Timed Out

- all OK, example is **a valid reservation**

### Not A Workday

- error message: OK
- order panel: OK
- calendar decoration: PENDING

### User is Supended

- error message: PENDING
- order panel: PENDING
- calendar decoration: TO BE DEFINED (we might want to show a normal order panel, but make it non-submitable)
