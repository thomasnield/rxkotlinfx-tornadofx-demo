package view

import domain.Customer
import domain.SalesPerson
import io.reactivex.subjects.BehaviorSubject
import tornadofx.Controller

class EventController: Controller() {
    val searchCustomers = BehaviorSubject.create<Set<Int>>()
    val searchCustomerUsages = BehaviorSubject.create<Set<Int>>()

    val applyCustomers = BehaviorSubject.create<Set<Int>>()
    val removeCustomerUsages = BehaviorSubject.create<Set<Int>>()

    val refreshSalesPeople = BehaviorSubject.create<Unit>()
    val refreshCustomers = BehaviorSubject.create<Unit>()

    val selectedCustomers = BehaviorSubject.create<Set<Customer>>()
    val selectedSalesPeople = BehaviorSubject.create<Set<SalesPerson>>()
    val selectedApplications = BehaviorSubject.create<Set<Int>>()

    val moveCustomerUp = BehaviorSubject.create<Int>()
    val moveCustomerDown = BehaviorSubject.create<Int>()

    val saveAssignments = BehaviorSubject.create<Unit>()

    val createNewCustomer = BehaviorSubject.create<Unit>()
    val deleteCustomers = BehaviorSubject.create<Set<Int>>()
    val deletedCustomers = BehaviorSubject.create<Set<Int>>()

    val createNewSalesPerson = BehaviorSubject.create<Unit>()
    val deleteSalesPerson = BehaviorSubject.create<Set<Int>>()
}