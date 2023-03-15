**Distributed Link State Routing Algorithm Implementation in Java** <br> <br>
This project is an implementation of the Distributed Link State Routing (DLSR) algorithm in Java. DLSR is a routing algorithm that uses a distributed database to keep track of the state of links and nodes in a network.

How it works: <br>
The algorithm works by each node broadcasting its own state to all of its neighbors. Each node maintains a database of all of its neighbor's states, and these states are then used to build a graph of the network. Once all of the nodes in the network have received and processed their neighbor's states, each node uses Dijkstra's algorithm to calculate the shortest path to all other nodes in the network.

Getting Started: <br>
Clone this repository to your local machine
Ensure that you have Java installed on your machine
Open the project in your preferred Java IDE
Compile and run the project
Usage
To use this implementation of the DLSR algorithm, you will need to create a network topology file. This file should contain the details of each node in the network, including its ID, IP address, and port number. An example network topology file is included in the project under network_topology.txt.

Once you have created your network topology file, you can run the project using the following command:

bash
Copy code
java DLSR network_topology.txt
This will start the DLSR algorithm on each node in the network. The program will output the routing table for each node, which will show the shortest path to all other nodes in the network.

Contributing: <br>
If you would like to contribute to this project, please create a pull request with your proposed changes.

License: <br>
This project is licensed under the MIT License - see the LICENSE file for details.
