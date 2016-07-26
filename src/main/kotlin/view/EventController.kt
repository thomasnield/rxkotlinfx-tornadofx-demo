package view

import domain.Customer
import domain.SalesPerson
import rx.javafx.sources.CompositeObservable
import tornadofx.Controller

class EventController: Controller() {
    val searchClients = CompositeObservable<Set<Int>>()
    val searchClientUsages = CompositeObservable<Set<Int>>()

    val applyCustomers = CompositeObservable<Set<Int>>()
    val removeCustomers = CompositeObservable<Set<Int>>()

    val refreshSalesPeople = CompositeObservable<Unit>()
    val refreshCustomers = CompositeObservable<Unit>()

    val selectedCustomers = CompositeObservable<Set<Customer>>(1) //cache last selection
    val selectedSalesPeople = CompositeObservable<Set<SalesPerson>>(1) //cache last selection
    val selectedApplications = CompositeObservable<Set<Int>>(1) //cache last selection
}