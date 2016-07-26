package view

import domain.Customer
import domain.SalesPerson
import rx.javafx.sources.CompositeObservable
import tornadofx.Controller

class EventController: Controller() {
    val searchCustomers = CompositeObservable<Set<Int>>()
    val searchCustomerUsages = CompositeObservable<Set<Int>>()

    val applyCustomers = CompositeObservable<Set<Int>>()
    val removeCustomers = CompositeObservable<Set<Int>>()

    val refreshSalesPeople = CompositeObservable<Unit>()
    val refreshCustomers = CompositeObservable<Unit>()

    val selectedCustomers = CompositeObservable<Set<Customer>>(1) //cache last selection
    val selectedSalesPeople = CompositeObservable<Set<SalesPerson>>(1) //cache last selection
    val selectedApplications = CompositeObservable<Set<Int>>(1) //cache last selection

    val moveSelectedCustomerUp = CompositeObservable<Int>()
    val moveSelectedCustomerDown = CompositeObservable<Int>()

    val saveAssignments = CompositeObservable<Unit>()
}