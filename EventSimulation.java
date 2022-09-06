import java.util.*;
import java.io.*;


//class declaration
public class EventSimulation
{
	//Declaring the simulation constants.
	final static int queue_limit = 100;
	final static int Busy = 1;
	final static int Idle = 0;
	private static int next_event_type, num_custs_delayed, num_delays_required,num_events, cust_in_queue, server_status;
    private static double area_cust_in_queue, area_server_status, mean_interarrival, mean_service, time, time_last_event,total_of_delays;
    private static double [] time_arrival = new double[ queue_limit+1 ];
    private static double [] time_next_event = new double[ 3 ]; 
    private static List<Double> doubles = new ArrayList<Double>();
    private static PrintWriter output; 
    // Initialization function.
    private static void initialize()
    {
    	//Initializing state variables and stastitical counters 
    	time = 0.0;
                  
    	server_status = Idle;
        cust_in_queue = 0;
        time_last_event = 0.0;

        
        num_custs_delayed = 0;
        total_of_delays = 0.0;
        area_cust_in_queue = 0.0;
        area_server_status = 0.0;
        
        // Initializing event list
        time_next_event[1] = time + expon(mean_interarrival);
        time_next_event[2] = Math.pow(10,30);
    }
    
    // Timing the next event 
    private static void timing()
    {
    	int i;
        double min_time_next_event = Math.pow(10,29);

        next_event_type = 0;

        // Determine the event type of the next event to occur.
        for (i = 1; i <= num_events; ++i) 
        {
        	if ((time_next_event[ i ] < min_time_next_event)) 
        	{
        		min_time_next_event = time_next_event[ i ];
        		next_event_type = i;
        	}	
        }

        // Checking whether event list is empty
        if (next_event_type == 0) 
        {
        	
            output.printf("\n%s%f","Event list empty at time: ", time);
            System.exit(1);	//End the simulation if empty 
        }

        // If not, advance the simulation clock.
        time = min_time_next_event;
    }

    // Arrival event function
    private static void arrive()
    {
    	double delay;

    	// Schedule the next arrival.
    	time_next_event[1] = time + expon(mean_interarrival);

    	// Check to see whether server is busy.
    	if (server_status == Busy) 
    	{
    		// Server is busy, so increment number of customers in queue.
    		++cust_in_queue;

    		//Checking for queue overflow
    		if (cust_in_queue > queue_limit) 
    		{
    			// If yes,stop the simulation
                output.printf("\n%s","Overflow of the array time_arrival at");
                output.printf("%s%f", " time: ", time );
    			System.exit(2);
    		}

    		// In the case of queue underflow, store the time of arrival of the arriving customer at the new end of time_arrival.
    		time_arrival[cust_in_queue] = time;
    	}
    	else 
		{
            //Server is idle, so arriving customer has a delay of zero.
            delay = 0.0;
            total_of_delays += delay;

            // Increment the number of customers delayed, and make server busy.
            ++num_custs_delayed;
            server_status = Busy;

            // Schedule a departure (service completion).
            time_next_event[2] = time + expon(mean_service);
		}
    }

    // Departure event function.
    private static void depart()
    {
    	int i;
        double delay;

        // Check to see whether the queue is empty.
        if (cust_in_queue == 0) 
        {
        	// The queue is empty so make the server idle and eliminate the departure (service completion) event from consideration.
        	server_status = Idle;
            time_next_event[2] = Math.pow(10,30);
        }
        else
        {
            // The queue is not empty, reduce the number of customers in queue.
        	--cust_in_queue;

        	// Compute the delay of the customer who is beginning service and update the total delay 
        	delay  = time - time_arrival[1];
            total_of_delays += delay;

            // Increase the number of customers delayed, and schedule departure
            ++num_custs_delayed;
            time_next_event[2] = time + expon(mean_service);

            // Move each customer in queue (if any) up one place.
            for (i = 1; i <= cust_in_queue; ++i) 
            {
            	time_arrival[i] = time_arrival[i + 1];
            }
        }
    }

    // Update area accumulators for time-average statistics.
    private static void update_time_avg_stats()
    {
    	double time_since_last_event;

    	// compute time since last event, and update the time marker for the last event 
    	time_since_last_event = time - time_last_event;
    	time_last_event = time;

    	// Update area under number-in-queue function.
    	area_cust_in_queue += cust_in_queue* time_since_last_event;

    	// Update area under server-busy indicator function.
    	area_server_status += server_status * time_since_last_event;
    }

    // Generating the exponential variate 
    private static double expon(double mean)
    {
    	double v;

    	
    	v = Math.random();

    	// Return an exponential random variate with mean "mean".
    	return -mean*Math.log(v);
    }
    
        // **************MAIN METHOD******************
    public static void main(String[] args) throws FileNotFoundException, IOException
    {
        // reading input paremeters from input files
        FileReader in = new FileReader("infile.txt");
        Scanner read = new Scanner(in);
        while (read.hasNext()) 
        {
            if (read.hasNextInt()) 
            {
            	// reading num_delays_required from input file
                num_delays_required = read.nextInt();
            } 
            else if (read.hasNextDouble()) 
            {
            	// reading mean_interarrival and mean_service and storing them into an Array
                double double_val = read.nextDouble();
                doubles.add(double_val);
            }    
        }
        read.close();
        // retrieving mean_interarrival and mean_service from ArrayList
        mean_interarrival = doubles.get(0);
        mean_service = doubles.get(1);
        
        // specify the number of events for the timing function.
        num_events = 2;

        // creating the input parameters file
        PrintWriter output = new PrintWriter("outfile.txt");

        // write report heading and input parameters into output files.
        output.printf("%s\n\n", "SINGLE-SERVER QUEUING SYSTEM");
        output.printf("%s%.1f%s\n", "Mean interarrival time: ", mean_interarrival, " minutes");
        output.printf("%s%.1f%s\n", "Mean service time: ", mean_service, " minutes");
        output.printf("%s%d\n", "Number of customers: ", num_delays_required);
        
        // Initialize the simulation.
        initialize();

        // Run the simulation while more delays are still needed.
        while (num_custs_delayed < num_delays_required)
        {
        	// Determine the next event.
        	timing();

        	// Update time-average statistical accumulators.
        	update_time_avg_stats();

        	// Invoke the appropriate event function.
        	switch (next_event_type)
        	{
        		case 1:
                    arrive () ;
                    break;
                case 2:
                    depart();
                    break;
        	}
        }
        // REPORT GENERATION
        output.printf("\n%s%.5f%s\n", "Average delay in queue: ", total_of_delays / num_custs_delayed, " minutes");
        output.printf("%s%.5f\n", "Average number in queue: ", area_cust_in_queue / time);
        output.printf("%s%.5f\n", "Server utilization: ", area_server_status / time);
        output.printf("%s%.5f", "Time simulation ended: ", time);

        output.close();
    }
} 
