package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db
       Train train=trainRepository.findById(bookTicketEntryDto.getTrainId()).get();
       
       String route=train.getRoute();

       int begIdx=route.indexOf(bookTicketEntryDto.getFromStation().toString());
       int endIdx=route.indexOf(bookTicketEntryDto.getToStation().toString());
       int bookings=0;
       for(Ticket ticket:train.getBookedTickets()){
            int startTkt=route.indexOf(ticket.getFromStation().toString());
            int endTkt=route.indexOf(ticket.getToStation().toString());
            if ((startTkt < endIdx && begIdx <= startTkt) ||
            (begIdx < endTkt && endTkt <= endIdx) ||
            (startTkt <= begIdx && endIdx <= endTkt)){
                bookings += ticket.getPassengersList().size();
            }
       }
       
       int availableSeats=train.getNoOfSeats()-bookings;
       if(bookTicketEntryDto.getNoOfSeats()>availableSeats){
        throw new Exception("Less tickets are available");
       }
      
       Ticket ticket=new Ticket();
       String[] routeArr=route.split(",");
       int st = -1;
        for(int i=0; i<routeArr.length; i++){
            if(routeArr[i].equals(bookTicketEntryDto.getFromStation().toString())){
                st = i;
                break;
            }
        }
        int ed = -1;
        for(int i=0; i<routeArr.length; i++){
            if(routeArr[i].equals(bookTicketEntryDto.getToStation().toString())){
                ed = i;
                break;
            }
        }
        if(st==-1 || ed==-1){
            throw new Exception("Invalid stations");
        }
       int fare=(ed-st)*300;
       ticket.setTotalFare(fare);
       ticket.setTrain(train);
       ticket.setFromStation(bookTicketEntryDto.getFromStation());
       ticket.setToStation(bookTicketEntryDto.getToStation());

       Passenger bookingPassenger=passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();
       bookingPassenger.getBookedTickets().add(ticket);
       ticket.setTrain(train);
       Ticket savedTicket=ticketRepository.save(ticket);

       train.getBookedTickets().add(ticket);
       trainRepository.save(train);
       return savedTicket.getTicketId();

    }
}
