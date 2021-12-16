# cart ux

## invalid reservations

NOTE: "calendar decoration" needs to be improved in general (e.g. colors for showing closed days before selecting them),
this is not mentioned in each example.

TODO:

- case for `models.maintenance_period` (likely not used at ZHdK, maybe ignore for now)

Description of expectations per invalidity condition:

- cart / refresh-timeout: existing item in the cart is marked invalid (red badge) when condition is true
- error message: "edit reservation" dialog has an understandable message explaining why the item is invalid
- order panel: "edit reservation" dialog does allow to fix the problem (by editing or deleting the item)
- calendar decoration:
- add to cart: the user can not create an invalid reservation when the condition is true beforehand

### Reservation Advance Days

- cart / refresh-timeout: OK
- error message: OK
- order panel: OK
- calendar decoration: OK
- add to cart: OK

### Start Date In Past

- cart / refresh-timeout: OK
- error message: OK
- order panel: OK
- calendar decoration: OK
- add to cart: OK

### No Access To Pool

- cart / refresh-timeout: OK
- error message: OK
- order panel: OK
- calendar decoration: OK (calendar hidden)
- add to cart: OK (pool not selectable)

### Holiday on End Date

- cart / refresh-timeout: OK
- error message: OK
- order panel: OK
- calendar decoration: OK
- add to cart: XXXXXXXXXXXX

### Max Visits Count Pick Up

- cart / refresh-timeout: OK
- error message: OK
- order panel: OK
- calendar decoration: OK
- add to cart: OK

### Max Visits Count Return

- cart / refresh-timeout: OK
- error message: OK
- order panel: OK
- calendar decoration: OK
- add to cart: OK

### Maximum Reservation Time

- cart / refresh-timeout: OK
- error message: OK
- order panel: OK
- calendar decoration: OK
- add to cart: OK

### Quantity Too High

- cart / refresh-timeout: OK
- error message: OK
- order panel: OK
- calendar decoration: OK
- add to cart: OK

### Model With No Items

- cart / refresh-timeout: OK
- error message: OK
- order panel: OK
- calendar decoration: OK (calendar hidden)
- add to cart: OK (pool not selectable)

### OK and Timed Out

- all OK, example is **a valid reservation**

### OK and Not Timed Out

- all OK, example is **a valid reservation**

### Not A Workday

- cart / refresh-timeout: OK
- error message: OK
- order panel: OK
- calendar decoration: OK
- add to cart: OK

### User is Supended

- cart / refresh-timeout: PENDING
- error message: OK
- order panel: OK
- calendar decoration: OK (calendar hidden)
- add to cart: OK
