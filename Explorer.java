package student;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;


import game.EscapeState;
import game.ExplorationState;
import game.Node;
import game.NodeStatus;

public class Explorer {

    /** Explore the cavern, trying to find the 
     * orb in as few steps as possible. Once you find the 
     * orb, you must return from the function in order to pick
     * it up. If you continue to move after finding the orb rather 
     * than returning, it will not count.
     * If you return from this function while not standing on top of the orb, 
     * it will count as a failure.
     * 
     * There is no limit to how many steps you can take, but you will receive
     * a score bonus multiplier for finding the orb in fewer steps.
     * 
     * At every step, you only know your current tile's ID and the ID of all 
     * open neighbor tiles, as well as the distance to the orb at each of these tiles
     * (ignoring walls and obstacles). 
     * 
     * In order to get information about the current state, use functions
     * getCurrentLocation(), getNeighbors(), and getDistanceToTarget() in ExplorationState.
     * You know you are standing on the orb when getDistanceToTarget() is 0.
     * 
     * Use function moveTo(long id) in ExplorationState to move to a neighboring 
     * tile by its ID. Doing this will change state to reflect your new position.
     * 
     * A suggested first implementation that will always find the orb, but likely won't
     * receive a large bonus multiplier, is a depth-first search.
     * 
     * @param state the information available at the current state
     */
	
    public void explore(ExplorationState state) {
        //TODO : Explore the cavern and find the orb
    	long startId = state.getCurrentLocation();
    	
    	//the path from the start node to the current node
    	//the start node is the last in the list, and the current node is the first
    	LinkedList<Long> path = new LinkedList<Long>();
    	path.addFirst(startId);
    	
    	//to record the nodes that has multiple neighbors
    	//they are the points where a decision has to be made
    	LinkedList<Long> mulNeighbor = new LinkedList<Long>();
    	
    	//to record all the nodes that have been visited in this graph
    	HashSet<Long> visited = new  HashSet<Long>();
    	visited.add(startId);
       	
    	dfs(state, path, mulNeighbor, visited); 
    }
    
    
    /**the recursion method to fulfill the looking-for mission.
     * when reaching a dead end, move back to where a choice was made for the last time;
     * when there is still a neighbor to visit, call the method recursively to make moves;
     * when standing on the orb, return.
     *
     * @param state: current information
     * @param path: a list of nodes walked along from the start
     * @param mulNeighbor: a list of nodes where choices were made
     * @param visited: all the nodes that have been visited in this graph
     */
    private static void dfs(ExplorationState state, LinkedList<Long> path, LinkedList<Long> mulNeighbor,
    		                HashSet<Long> visited){
    	//base case: found the orb, return
    	if (state.getDistanceToTarget() == 0) return;
    	
    	//all the unvisited neighbors of the current node
    	ArrayList<NodeStatus> unvisitedNeighbor = new ArrayList<NodeStatus>();
    	
    	Collection<NodeStatus> neighbors = state.getNeighbors();
    	for (NodeStatus neighbor : neighbors){
        	if (!visited.contains(neighbor.getId()))
        		unvisitedNeighbor.add(neighbor);
        }
                
        int count = unvisitedNeighbor.size();
        
        //dead-end: go back to where a choice was made for the last time
        if (count == 0){
        	goBack(path, mulNeighbor, state);
    		
        	path.addFirst(state.getCurrentLocation());	
    		mulNeighbor.pollFirst();
    		
    		dfs(state, path, mulNeighbor, visited);
        }
        
        //still have neighbors to visit
        else {
        	long neighborId;
        	//only one neighbor able to visit: no choice
        	if (count == 1)
        		neighborId = unvisitedNeighbor.get(0).getId();
        	//multiple neighbors able to visit
        	else{
        		neighborId = nearestNeighbor(unvisitedNeighbor);
        		mulNeighbor.addFirst(state.getCurrentLocation());
        	}
        	
        	//make a adjacent move and record
        	state.moveTo(neighborId);
        	path.addFirst(neighborId);       	
        	visited.add(neighborId); 
  
        	//do recursion to make the next move
        	dfs(state, path, mulNeighbor, visited);      
        }
    }
    
    
    /** return the Id of the unvisited neighbor who has the shorted Manhattan distance of all the 
     * unvisited neighbors passed in.
     * 
     * @param unvisitedNeighbor: all the unvisitedNeighbors of current node
     * @return the Id of the unvisited neighbor who has the shorted Manhattan distance of all the 
     *         unvisited neighbors
     */
    private static long nearestNeighbor(ArrayList<NodeStatus> unvisitedNeighbor){
    	NodeStatus nearest = unvisitedNeighbor.get(0);
    	for (int i = 1; i < unvisitedNeighbor.size(); i++){
    		if (unvisitedNeighbor.get(i).compareTo(nearest) < 0)
    			nearest = unvisitedNeighbor.get(i);
    	}
    	return nearest.getId();
    } 
    
    
    
    /**go back to where a choice has to be made for the last time.
     * 
     * @param path: a list of nodes walked along from start
     * @param mulNeighbor: a list of node where choices were made
     * @param state: current information
     */
    private static void goBack(LinkedList<Long> path, LinkedList<Long> mulNeighbor, ExplorationState state){
    	//throw away the current node in the path
    	path.pollFirst();
    	
    	//move back to where a choice was made for the last time
    	//after iteration, standing on the node where choice made
    	while (state.getCurrentLocation() != mulNeighbor.peekFirst()){
    		long back = path.pollFirst();
    		state.moveTo(back);
    	}  	
    }
      

    /**  Escape from the cavern before the ceiling collapses, trying to collect as much
     * gold as possible along the way. Your solution must ALWAYS escape before time runs
     * out, and this should be prioritized above collecting gold.
     * 
     * You now have access to the entire underlying graph, which can be accessed through EscapeState.
     * getCurrentNode() and getExit() will return you Node objects of interest, and getVertices()
     * will return a collection of all nodes on the graph. 
     * 
     * Note that time is measured entirely in the number of steps taken, and for each step
     * the time remaining is decremented by the weight of the edge taken. You can use
     * getTimeRemaining() to get the time still remaining, pickUpGold() to pick up any gold
     * on your current tile (this will fail if no such gold exists), and moveTo() to move
     * to a destination node adjacent to your current node.
     * 
     * You must return from this function while standing at the exit. Failing to do so before time
     * runs out or returning from the wrong location will be considered a failed run.
     * 
     * You will always have enough time to escape using the shortest path from the starting
     * position to the exit, although this will not collect much gold. But for this reason, using 
     * Dijkstra's to plot the shortest path to the exit is a good starting solution.
     * 
     * @param state the information available at the current state
     */
    
	public void escape(EscapeState state) {
    	search(state);  
    }
	
	
	/** the recursion method to fulfill the escape mission.
	 * when reaching the exist node, return;
	 * when no neighbors are walkable, walk to exist directly;
	 * when at least one neighbor is walkable, make a choice for the next step, walk to that;
	 * call the method recursively to make moves
	 *
	 * @param state: current information
	 */
	private static void search(EscapeState state){
		//base case: reach the exist
		if (state.getCurrentNode().equals(state.getExit())) return;
		
		//a list of walkable neighbors of the current node
		ArrayList<Node> walkableNeighbors = new ArrayList<Node>();       	
       	
		Collection<Node> neighbors = state.getCurrentNode().getNeighbors();
		for (Node neighbor : neighbors){
			if (walkable(state, neighbor))
					walkableNeighbors.add(neighbor);
		}
		
		//when no neighbors are walkable, walk to exist directly
		if (walkableNeighbors.isEmpty()){			
			walkTo(state, state.getExit());
    		search(state);
    	} 
		
		//at least one neighbor is walkable
    	else{
    		//make a decision for the next move, walk to it
    		Node bestMove = bestMove(walkableNeighbors, state);
    		walkTo(state, bestMove);
    		search(state);
    	}   		
	}
	
	
	/**use this method to make a reasonable decision for the next move when there is at least 
	 * one walkable neighbor
	 * for all the walkable neighbors, visit the one that has the most gold;
	 * if all the walkable neighbors have no gold, visit the nearest walkable node with gold
	 * if all the walkable nodes from this node have no gold, select one walkable neighbor to visit
	 *
	 * precondition: list of walkable neighbors has at least one element
	 * @param walkableNeighbors: all the walkable neighbors of current node
	 * @param state: current information
	 * @return the node that is decided to move to 
	 */
	private static Node bestMove(ArrayList<Node> walkableNeighbors, EscapeState state){
		//sort all the walkable neighbors by the gold amount in descending order
		sortNodesGold(walkableNeighbors);
		
		//when all the walkable neighbors have no gold
		if (walkableNeighbors.get(0).getTile().getGold() == 0) {			
			//get a min-heap of walkable nodes from current node
			//heap is decided by the distance to the current node
			Collection<Node> vertices = state.getVertices();
			Heap<Node> sortedWalkableNodesDistance = sortedWalkableNodesDistance(vertices, state);
		
			//find the walkable && having gold node with as short as possible distance to the current node
			while(!sortedWalkableNodesDistance.isEmpty()){
				Node nearestNode = sortedWalkableNodesDistance.poll();
				
				if (nearestNode.getTile().getGold() != 0)
						return nearestNode;				
			}
		}
		
		//visit the walkable neighbor with the most gold
		//or 
		//if all the walkable neighbors have no gold && all the walkable nodes from current node
		//have no gold, visit the first element in the list of walkable neighbors
		return walkableNeighbors.get(0);
	}
	
	
    /** move from current node to the destination node
     * when walking along the path, if there is gold on the path, pick it up
     * 
     * @param state: current information
     * @param destination: the node heading to 
     */
    private static void walkTo(EscapeState state, Node destination){
    	//when current node = destination node, just pick up gold
    	if (state.getCurrentNode().equals(destination)){
    		if (destination.getTile().getGold() > 0)
    			state.pickUpGold();
    	} 
    	
    	//when there are at least two elements in the path
    	java.util.List<Node> path = Paths.dijkstra(state.getCurrentNode(), destination);
   
    	for (int i = 1; i < path.size(); i++){
    		state.moveTo(path.get(i));
    		if (path.get(i).getTile().getGold() > 0)
    			state.pickUpGold();
    	}  	
    }
    
    
    /** to judge if it is executable to move to the destination node.
     * if after moving to the destination node, there isn't enough left time to escape from this 
     * destination node, return false;
     * else, return true.
     * 
     * @param state: current information
     * @param destination: the node intended to head to 
     * @return
     */
    private static boolean walkable(EscapeState state, Node destination){
    	//calculate the remaining limited time after moving to the destination node
    	java.util.List<Node> path = Paths.dijkstra(state.getCurrentNode(), destination);
    	int pathLength = Paths.pathLength(path); 	
    	int timeRemaining = state.getTimeRemaining() - pathLength;
    	
    	//calculate the shortest time needed to escape from the destination node
    	java.util.List<Node> pathToExist = Paths.dijkstra(destination, state.getExit());
    	int exsitTime = Paths.pathLength(pathToExist); 	
  
    	if (exsitTime > timeRemaining)
    		return false;
    	return true;
    }
    
    
    /** sort the nodes by the amount of gold in it in descending order
     * 
     * @param nodes: nodes to be sorted
     */
    private static void sortNodesGold(ArrayList<Node> nodes){
    	Collections.sort(nodes, new Comparator<Node>(){
			@Override
			public int compare(Node node1, Node node2) {
				return node2.getTile().getGold() - node1.getTile().getGold();
			}			
		});
    }
    
    
    /** sort the nodes by the distance between this node and the current node in increasing order
     * if one node is not walkable from current node, discard it
     * return a min-heap of walkable nodes from current node which  is decided by the distance to 
     * the current node 
     * 
     * @param nodes: nodes to be sorted
     * @param state: current information
     * @return a min-heap of walkable nodes from current node which  is decided by the distance to 
     *         the current node 
     */
    private static Heap<Node> sortedWalkableNodesDistance(Collection<Node> nodes, EscapeState state){
    	Heap<Node> sortedWalkableNodes = new Heap<Node>();
    	for (Node node : nodes){
    		if (walkable(state, node)){
    			java.util.List<Node> path = Paths.dijkstra(state.getCurrentNode(), node);
            	int pathLength = Paths.pathLength(path);
                sortedWalkableNodes.add(node, pathLength);
    		}
    	}  	
    	return sortedWalkableNodes;
    }	

}
