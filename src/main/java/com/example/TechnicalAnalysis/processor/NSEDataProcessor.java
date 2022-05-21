package com.example.TechnicalAnalysis.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.TechnicalAnalysis.entity.Data;
import com.example.TechnicalAnalysis.entity.FilteredResponse;
import com.example.TechnicalAnalysis.entity.NSE;

@Component
public class NSEDataProcessor {

	long niftyCount=0;	
	List<Data> niftyPastRecords= new ArrayList<Data>();
	
	long bankNiftyCount=0;	
	double crore = 10000000;
	List<Data> bankNiftyPastRecords= new ArrayList<Data>();
	
	public FilteredResponse processNiftyData(NSE nse, double limit, double round) {
		
		HashMap<Double, Double> putSortedLevels = new HashMap<>();
		HashMap<Double, Double> callSortedLevels = new HashMap<>();
		
		HashMap<Double, Double> putLevels = new HashMap<>();
		HashMap<Double, Double> callLevels = new HashMap<>();
			
			List<Data> records = new ArrayList<Data>();
			
			double pastUnderlyingPrice =0;


			//Calculate Current Strike Price
			double currentStrike =Math.round((nse.getRecords().getUnderlyingValue()/round))*round;


			//Todo- Get the current expiry date and next date from calendar.Then calculate the same for next expiry date.

			//Filter Out the data for 10 records above and less
			records = Arrays.asList(nse.getFiltered().getData()).stream()
					.filter(data -> (data.getStrikePrice()>=(currentStrike-limit) && data.getStrikePrice()<=(currentStrike+limit)))
					.collect(Collectors.toList());


			// Calculating Support and Resistance
			for (Data data : records) {
				putLevels.put(data.getStrikePrice(), (data.getPe().getChangeinOpenInterest()* data.getPe().getTotalTradedVolume())/crore);
				callLevels.put(data.getStrikePrice(), (data.getCe().getChangeinOpenInterest()* data.getCe().getTotalTradedVolume())/crore);
			}
			
			putLevels.entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.limit(5)
				.forEachOrdered(x -> putSortedLevels.put(x.getKey(), x.getValue()));
			
			callLevels.entrySet().stream()
			.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
			.limit(5)
			.forEachOrdered(x -> callSortedLevels.put(x.getKey(), x.getValue()));
			
			if(niftyCount==0) {
				niftyPastRecords = records;
			}
			
			FilteredResponse response = new FilteredResponse();

			response.setUnderlyingPrice(nse.getRecords().getUnderlyingValue());
			response.setCurrentStrike(currentStrike);
			response.setPastRecords(niftyPastRecords);
			response.setPastUnderlyingPrice(pastUnderlyingPrice);
			response.setRecords(records);
			response.setCallLevels(callSortedLevels);
			response.setPutLevels(putSortedLevels);	
			niftyPastRecords = records;
			pastUnderlyingPrice=nse.getRecords().getUnderlyingValue();
			response.setPutTotalOI(nse.getFiltered().getPe().getTotOI());
			response.setCallTotalOI(nse.getFiltered().getCe().getTotOI());
			response.setPutTotalVolume(nse.getFiltered().getPe().getTotVol());
			response.setCallTotalVolume(nse.getFiltered().getCe().getTotVol());
			response.setTotalChangeInOIForCall(records.stream().map(data -> data.getCe().getChangeinOpenInterest()).collect(Collectors.toList()).stream().reduce(0.0, Double::sum));
	 	    response.setTotalChangeInOIForPut(records.stream().map(data -> data.getPe().getChangeinOpenInterest()).collect(Collectors.toList()).stream().reduce(0.0, Double::sum));
			response.setPcr(Math.abs(response.getTotalChangeInOIForPut()/response.getTotalChangeInOIForCall()));
		niftyCount++;
			
			return response;
		}
	
	public FilteredResponse processBankNiftyData(NSE nse, double limit, double round) {
		
		HashMap<Double, Double> putSortedLevels = new HashMap<>();
		HashMap<Double, Double> callSortedLevels = new HashMap<>();
		
		HashMap<Double, Double> putLevels = new HashMap<>();
		HashMap<Double, Double> callLevels = new HashMap<>();
		
		List<Data> records = new ArrayList<Data>();
		
		double pastUnderlyingPrice =0;

		double currentStrike =Math.round((nse.getRecords().getUnderlyingValue()/round))*round;
		
		records = Arrays.asList(nse.getFiltered().getData()).stream()
				.filter(data -> (data.getStrikePrice()>=(currentStrike-limit) && data.getStrikePrice()<=(currentStrike+limit)))
				.collect(Collectors.toList());
		
		
		for (Data data : records) {
			putLevels.put(data.getStrikePrice(), (data.getPe().getChangeinOpenInterest()* data.getPe().getTotalTradedVolume())/crore);
			callLevels.put(data.getStrikePrice(), (data.getCe().getChangeinOpenInterest()* data.getCe().getTotalTradedVolume())/crore);
		}
		
		putLevels.entrySet().stream()
			.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
			.limit(5)
			.forEachOrdered(x -> putSortedLevels.put(x.getKey(), x.getValue()));
		
		callLevels.entrySet().stream()
		.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
		.limit(5)
		.forEachOrdered(x -> callSortedLevels.put(x.getKey(), x.getValue()));
		
		if(bankNiftyCount==0) {
			bankNiftyPastRecords = records;
		}
		
		FilteredResponse response = new FilteredResponse();
		response.setUnderlyingPrice(nse.getRecords().getUnderlyingValue());
		response.setCurrentStrike(currentStrike);
		response.setPastRecords(bankNiftyPastRecords);
		response.setPastUnderlyingPrice(pastUnderlyingPrice);
		response.setRecords(records);
		response.setCallLevels(callSortedLevels);
		response.setPutLevels(putSortedLevels);	
		response.setPutTotalOI(nse.getFiltered().getPe().getTotOI());
		response.setCallTotalOI(nse.getFiltered().getCe().getTotOI());
		response.setPutTotalVolume(nse.getFiltered().getPe().getTotVol());
		response.setCallTotalVolume(nse.getFiltered().getCe().getTotVol());
		bankNiftyPastRecords = records;
		pastUnderlyingPrice=nse.getRecords().getUnderlyingValue();
		response.setTotalChangeInOIForCall(records.stream().map(data -> data.getCe().getChangeinOpenInterest()).collect(Collectors.toList()).stream().reduce(0.0, Double::sum));
		response.setTotalChangeInOIForPut(records.stream().map(data -> data.getPe().getChangeinOpenInterest()).collect(Collectors.toList()).stream().reduce(0.0, Double::sum));
		response.setPcr(Math.abs(response.getTotalChangeInOIForPut()/response.getTotalChangeInOIForCall()));

		bankNiftyCount++;
		
		return response;
	}

}
