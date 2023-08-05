package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library
        Train newTrain=new Train();
        newTrain.setDepartureTime(trainEntryDto.getDepartureTime());
        newTrain.setNoOfSeats(trainEntryDto.getNoOfSeats());
        List<Station> listOfRoute=trainEntryDto.getStationRoute();
        String route="";
        for(int i=0;i<listOfRoute.size()-1;i++){
            route+=listOfRoute.get(i)+",";
        }
        route+=listOfRoute.get(listOfRoute.size()-1);
        newTrain.setRoute(route);
        Train savedTrain=trainRepository.save(newTrain);

        return savedTrain.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.
        int availableSeats=0;
        String fromStation=seatAvailabilityEntryDto.getFromStation().toString();
        String toStation=seatAvailabilityEntryDto.getToStation().toString();
        Train train=trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();
        String route=train.getRoute();
        
        int startStation=route.indexOf(fromStation);
        int endStation=route.indexOf(toStation);

        int bookings=0;
        List<Ticket> bookTickets=train.getBookedTickets();
        for(Ticket ticket:bookTickets){
            int startTicket=route.indexOf(ticket.getFromStation().toString());
            int endTicket=route.indexOf(ticket.getToStation().toString());
            if((startTicket<endStation && startStation<=startTicket) ||(
                startTicket<=startStation && endTicket<=endStation) ||(
                    startTicket<=startStation && endTicket<=endStation)
            ){
            bookings+=ticket.getPassengersList().size();
                }
            

        }
        availableSeats=train.getNoOfSeats()-bookings;

        
        return availableSeats;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.
        Train train=trainRepository.findById(trainId).get();
        String route = train.getRoute();
        int stationIdx = route.indexOf(station.toString());

        if(stationIdx == -1){
            throw new Exception("Train is not passing from this station");
        }

        int countOfPassengers=0;

        List<Ticket> bookings=train.getBookedTickets();
        for(Ticket t:bookings){
            Station currStation=t.getFromStation();
            if(currStation.equals(station)){
                countOfPassengers+=t.getPassengersList().size();
             
            }
        }
        
        return countOfPassengers;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0
         
        Train train=trainRepository.findById(trainId).get();
        int age=0;
        List<Ticket> bookings=train.getBookedTickets();
        for(Ticket t:bookings){
           
            List<Passenger> listOfPassengers=t.getPassengersList();
           
            for(Passenger p:listOfPassengers){
                if(p.getAge()>age)
                    age=p.getAge();
            }
        

        }
        return age;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.
        List<Train> allTrains=trainRepository.findAll();
        List<Integer> trainIds = new ArrayList<>();
        for(Train train: allTrains){
            String route=train.getRoute();
            int idx=route.indexOf(station.toString());
            if(idx==-1){
                continue;
            }
            String[] routeArr = route.split(",");
            int stationIdx = 0;
            for(int i=0; i<routeArr.length;i++){
                if(routeArr[i].equals(station.toString())){
                    stationIdx = i;
                }
            }
            LocalTime time=train.getDepartureTime().plusHours(stationIdx);
            if(time.compareTo(startTime)>=0 && time.compareTo(endTime)<=0){
                trainIds.add(train.getTrainId());
            }

        }
        return trainIds;
    }

}
