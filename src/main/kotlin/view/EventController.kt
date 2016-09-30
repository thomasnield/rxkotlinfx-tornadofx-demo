package view

import domain.ClientCompany
import domain.SalesPerson
import rx.javafx.sources.CompositeObservable
import tornadofx.Controller

class EventController: Controller() {
    val searchClients = CompositeObservable<List<Int>>()
    val searchClientUsages = CompositeObservable<List<Int>>()

    val applyClients = CompositeObservable<List<Int>>()
    val removeClients = CompositeObservable<List<Int>>()

    val refreshSalesPeople = CompositeObservable<Unit>()
    val refreshCompanyClients = CompositeObservable<Unit>()

    val selectedClients = CompositeObservable<List<ClientCompany>> { it.replay(1).autoConnect() }  //cache last selection
    val selectedSalesPeople = CompositeObservable<List<SalesPerson>> { it.replay(1).autoConnect() } //cache last selection

}