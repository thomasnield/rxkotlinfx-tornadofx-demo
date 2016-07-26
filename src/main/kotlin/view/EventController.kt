package view

import domain.ClientCompany
import domain.SalesPerson
import rx.javafx.sources.CompositeObservable
import tornadofx.Controller

class EventController: Controller() {
    val searchClients = CompositeObservable<Set<Int>>()
    val searchClientUsages = CompositeObservable<Set<Int>>()

    val applyClients = CompositeObservable<Set<Int>>()
    val removeClients = CompositeObservable<Set<Int>>()

    val refreshSalesPeople = CompositeObservable<Unit>()
    val refreshCompanyClients = CompositeObservable<Unit>()

    val selectedClients = CompositeObservable<Set<ClientCompany>>(1) //cache last selection
    val selectedSalesPeople = CompositeObservable<Set<SalesPerson>>(1) //cache last selection

}