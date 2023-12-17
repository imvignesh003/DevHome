package com.devHome.Home.Controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * @author Vignesh G
 */
@Controller
public class HomeController {
    
    @RequestMapping("/")    
    public String home(){
        return "index.html";
    }

    @RequestMapping("/estimation")
    public String estimator(){
        return "Estimator.html";
    }
    

    @RequestMapping("/calculate")
    public String calc(
                        @RequestParam String quality,
                        @RequestParam String sqftString,
                        @RequestParam String floor,
                        @RequestParam boolean woodwork,
                        @RequestParam String state,
                        @RequestParam boolean subsidy,
                        @RequestParam(required = false) String incomeString,
                        @RequestParam(required = false) String amountString,
                        @RequestParam(required = false) String tenureString,
                        @RequestParam(required = false) boolean puccaHouse,
                        Model model) {
        int sqft = Integer.parseInt(sqftString);
        System.out.println("Quality: "+quality + " Sqft: "+sqft + " Floor: "+floor + " Woodwork: "+woodwork  + " State: "+state + " Subsidy: "+subsidy + " Income: "+incomeString + " Amount: "+amountString + " Tenure: "+tenureString + " Pucca House: "+puccaHouse);
        double inr = 0;
        final String loadedCategory = "Loaded";
        final String economyCategory = "Economy";
        final String premiumCategory = "Premium";
        final String luxuryCategory = "Luxury";
        
        final String granites = "Granites";
        final String marbles = "Marbles";

        if(floor.equalsIgnoreCase(granites)){
            inr += 50;
        }else if(floor.equalsIgnoreCase(marbles)){
            inr += 30;
        }


        if(quality.equalsIgnoreCase(loadedCategory)){
            inr += 1000;
        }
        else if(quality.equalsIgnoreCase(economyCategory)){
            inr += 1700;
        }
        else if(quality.equalsIgnoreCase(premiumCategory)){
            inr += 2300;
        }
        else if(quality.equalsIgnoreCase(luxuryCategory)){
            inr += 3000;
        }
        double supervisor = inr * (5/100.0);
        double labour = inr * (20/100.0);
        double total = (inr + supervisor + labour)*sqft;
        System.out.println(total);
        double sub = 0;
        if(subsidy){
            sub = calculate(Double.parseDouble(incomeString), Double.parseDouble(amountString), Integer.parseInt(tenureString), puccaHouse);
            System.out.println(sub);
        }   
        model.addAttribute("total", total);
        model.addAttribute("subsidy", sub==0 || !subsidy?"Not Eligible":sub);
        model.addAttribute("final", sub == 0?total:total-sub);
        return "output.html";
    }

    private double calculate(double annualIncome, double loanAmount, int tenure, boolean puccaHouse/* , String isWomenProperty, String isWomen, double carpetArea*/) {
        final String migi="MIG-I";
        final String migii="MIG-II"; 
        final String ewslig="EWS/LIG";
        String typeOfScheme = determineScheme(annualIncome);
        double subsidy = 0;
        if (puccaHouse) {
            
            System.out.println(typeOfScheme);

            if (migi.equals(typeOfScheme) || migii.equals(typeOfScheme)) {
                subsidy = calculateSubsidy(annualIncome,loanAmount, typeOfScheme,tenure);
             }
             else if (ewslig.equals(typeOfScheme)) {
                subsidy = calculateSubsidy(annualIncome,loanAmount, typeOfScheme,tenure);
            }
        } else {
            System.out.println("Not Eligible in PMAY");
        }
        return subsidy;
    }
    
    private static final double EWS_LIG_THRESHOLD = 600000;
    private static final double MIG1_THRESHOLD = 1200000;
    private static final double MIG2_THRESHOLD = 1800000;

    private static String determineScheme(double annualIncome) {
        if (annualIncome <= EWS_LIG_THRESHOLD) {
            return "EWS/LIG";
        } else if (annualIncome <= MIG1_THRESHOLD) {
            return "MIG-I";
        } else if (annualIncome <= MIG2_THRESHOLD) {
            return "MIG-II";
        } else {
            return "Not Eligible in PMAY";
        }
    }
    
    
    private double calculateSubsidy(/*double carpetArea,*/ double annualIncome, double loanAmount, String typeOfScheme, int tenure) {
        double minews = 600000;
        double minmig1 = 900000;
        double minmig2 = 1200000;

        int n = Math.min(240, tenure);
    
        double loan = 0;
        double roi = 0;
    
        final String migii = "MIG-II";
        final String ewslig = "EWS/LIG";
        final String migi = "MIG-I";
    
        if (ewslig.equals(typeOfScheme)) {
            loan = Math.min(minews, loanAmount);
            roi = 6.5;
        } else if (migi.equals(typeOfScheme)) {
            loan = Math.min(minmig1, loanAmount);
            roi = 4;
        } else if (migii.equals(typeOfScheme)) {
            loan = Math.min(minmig2, loanAmount);
            roi = 3;
        }
    
        double emi = calculateEmi(loan, roi, n);
        double finalnpv = calculateFinalNpv(emi, loan, roi, n);
    
        return finalnpv;
    }

    private double calculateEmi(double loanAmount, double interestRate, int tenure) {
        int n = Math.min(240, tenure);
        double monthlyInterestRate = interestRate / 1200;
        double denominator = 1 - Math.pow((1 + monthlyInterestRate), -n);
        double emi = loanAmount * (monthlyInterestRate / denominator);
        return emi;
    }

    
    private double calculateFinalNpv(double emi, double loanAmount, double interestRate, int tenure) {
        int n = Math.min(240, tenure);
        int counter = 1;
        double outstandingLoan = loanAmount;
        double finalNpv = 0;
    
        while (n >= counter) {
            double interestForTheMonth = outstandingLoan * (interestRate / 1200);
            double principalPaid = emi - interestForTheMonth;
            outstandingLoan -= principalPaid;
            double npv = interestForTheMonth / Math.pow((1 + (9 / 1200)), counter);
            finalNpv += npv;
            counter += 1;
        }
    
        return Math.round(finalNpv);
    }

    @RequestMapping("/time")
    public String testim() {
        return "time.html";
    }

    @RequestMapping("/timecalculate")
    public String tcalc(@RequestParam String date, Model model){
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/YYYY");
        LocalDate dat = LocalDate.parse(date);
        String d = dat.format(formatter);
        System.out.println(d);
        LocalDate[][] dates = new LocalDate[17][2];
        dates[0][0] = dat;
        dates[0][1] = dat.plusDays(10);
        dates[1][0] = dates[0][1].plusDays(1);
        dates[1][1] = dates[1][0].plusDays(7);
        dates[2][0] = dates[1][1].plusDays(1);
        dates[2][1] = dates[2][0].plusDays(30);
        dates[3][0] = dates[2][1].plusDays(1);
        dates[3][1] = dates[3][0].plusDays(20);
        dates[4][0] = dates[3][1].plusDays(1);
        dates[4][1] = dates[4][0].plusDays(14);
        dates[5][0] = dates[4][1].plusDays(1);
        dates[5][1] = dates[5][0].plusDays(3);
        dates[6][0] = dates[5][1].plusDays(1);
        dates[6][1] = dates[6][0].plusDays(7);
        dates[7][0] = dates[6][1].plusDays(1);
        dates[7][1] = dates[7][0].plusDays(14); 
        dates[8][0] = dates[7][1].plusDays(1);
        dates[8][1] = dates[8][0].plusDays(10);
        dates[9][0] = dates[8][1].plusDays(1);
        dates[9][1] = dates[9][0].plusDays(16);
        dates[10][0] = dates[9][1].plusDays(1);
        dates[10][1] = dates[10][0].plusDays(20);
        dates[11][0] = dates[10][1].plusDays(1);
        dates[11][1] = dates[11][0].plusDays(13);
        dates[12][0] = dates[11][1].plusDays(1);
        dates[12][1] = dates[12][0].plusDays(23);
        dates[13][0] = dates[12][1].plusDays(1);
        dates[13][1] = dates[13][0].plusDays(9);
        dates[14][0] = dates[13][1].plusDays(1);
        dates[14][1] = dates[14][0].plusDays(9);
        dates[15][0] = dates[14][1].plusDays(1);
        dates[15][1] = dates[15][0].plusDays(9);
        dates[16][0] = dates[15][1].plusDays(1);

        model.addAttribute("dates", dates);
        return "time-output.html";
    }
    
    
    
    @RequestMapping("/mldashboard")
    public ResponseEntity<List<String>> mldash() {
        // ...

        List<String> pred = new ArrayList<String>();
        // DataSource source = new DataSource("C:/Users/DELL/Downloads/esan.arff");
        // Instances data = source.getDataSet();

        // // Create the J48 classifier
        // J48 j48 = new J48();

        // // Train the classifier
        // j48.buildClassifier(data);

        // // Evaluate the classifier
        // Evaluation eval = new Evaluation(data);
        // eval.evaluateModel(j48, data);

        // // Print the results
        // System.out.println(eval.toSummaryString());
 
        return new ResponseEntity<List<String>>(pred, HttpStatus.OK);
    }
    

    // @RequestMapping("/error")
    // public String error(){
    //     return "Error.html";
    // }
}
