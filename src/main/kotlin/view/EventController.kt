package view

import domain.Customer
import domain.SalesPerson
import rx.javafx.sources.CompositeObservable
import tornadofx.Controller

class EventController: Controller() {
    val searchCustomers = CompositeObservable<Set<Int>>()
    val searchCustomerUsages = CompositeObservable<Set<Int>>()

    val applyCustomers = CompositeObservable<Set<Int>>()
    val removeCustomerUsages = CompositeObservable<Set<Int>>()

    val refreshSalesPeople = CompositeObservable<Unit>()
    val refreshCustomers = CompositeObservable<Unit>()

    val selectedCustomers = CompositeObservable<Set<Customer>> { it.replay(1).autoConnect() }
    val selectedSalesPeople = CompositeObservable<Set<SalesPerson>> { it.replay(1).autoConnect() }
    val selectedApplications = CompositeObservable<Set<Int>> { it.replay(1).autoConnect() }

    val moveCustomerUp = CompositeObservable<Int>()
    val moveCustomerDown = CompositeObservable<Int>()

    val saveAssignments = CompositeObservable<Unit>()

    val createNewCustomer = CompositeObservable<Unit>()
    val deleteCustomers = CompositeObservable<Set<Int>>()
    val deletedCustomers = CompositeObservable<Set<Int>>()

    val createNewSalesPerson = CompositeObservable<Unit>()
    val deleteSalesPerson = CompositeObservable<Set<Int>>()
}