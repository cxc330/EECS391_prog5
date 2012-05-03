package edu.cwru.SimpleRTS.agent;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.io.*;

import edu.cwru.SimpleRTS.action.*;
import edu.cwru.SimpleRTS.environment.State.StateView;
import edu.cwru.SimpleRTS.model.Template.TemplateView;
import edu.cwru.SimpleRTS.model.resource.ResourceNode.ResourceView;
import edu.cwru.SimpleRTS.model.resource.ResourceNode.Type;
import edu.cwru.SimpleRTS.model.resource.ResourceType;
import edu.cwru.SimpleRTS.model.unit.Unit.UnitView;
import edu.cwru.SimpleRTS.util.DistanceMetrics;

public class ReinforcementAgent extends Agent {

	private static final long serialVersionUID = 1L;
	static int playernum = 0;
	static String townHall = "TownHall";
	static String peasant = "Peasant";
	static String footmen = "Footman";
	static String gather = "gather";
	static String deposit = "deposit";

	private List<Integer> footmenIDs = new ArrayList<Integer>();
	private List<Integer> myFootmen = new ArrayList<Integer>();
	private List<Integer> enemyFootmen = new ArrayList<Integer>();
	private HashMap<Integer, Integer> healths = new HashMap<Integer, Integer>();
	private int episodeNum = 10;
	private int episodeCount = 0;
	private int turnCount = 0;
	private int initSize = 0;
	private int totalTurns = 0;
	private List<ArrayList<turnInstance>> attackRewardsMap = new ArrayList<ArrayList<turnInstance>>();
	private int myfootmanStartHealth, myfootmanStartCount, enemyfootmanStartHealth, enemyfootmanStartCount;
	private static int footmanDeathWorth = 100;
	private int totalScore = 0;
	
	// Constructor
	public ReinforcementAgent(int playernum, String[] args) throws FileNotFoundException, IOException, BackingStoreException{
		super(playernum);
		episodeNum = Integer.parseInt(args[0]);
		episodeNum = (episodeNum/10) * 15 + (episodeNum%10);
		Preferences prefs = Preferences.userRoot().node("edu").node("cwru").node("SimpleRTS");
		configureEnvPrefs(prefs);
		configureModelPrefs(prefs);
		prefs.exportSubtree(System.out);
		prefs.exportSubtree(new FileOutputStream("data/defaultConfig.xml"));
	}

	// Below is the initialStep and setup for our agent
	public Map<Integer, Action> initialStep(StateView state) {
		attackRewardsMap.add(new ArrayList<turnInstance>());
		//Updates the list of footmen
		//updateFootmen(state);
		return middleStep(state);
	}

	// Below is the middleStep function where the majority of the logic works
	public Map<Integer, Action> middleStep(StateView state) {
		Map<Integer, Action> actions = new HashMap<Integer, Action>(); //actions to return
		List<attackInstance> attacks = new ArrayList<attackInstance>(); //List of attackInstances to store in the "rewardsMap"
		
		//Updates the list of footmen
		updateFootmen(state);
		//Print health
		
		if(turnCount == 0) { //Calculating initial values for rewards calculations
			//resetting the values to 0
			myfootmanStartHealth = 0; enemyfootmanStartHealth = 0;
			//getting the count of the footmen for each side
			myfootmanStartCount = myFootmen.size();
			enemyfootmanStartCount = enemyFootmen.size();
			//calculating total starting HP for each side
			for( Integer id : myFootmen) { myfootmanStartHealth += state.getUnit(id).getHP(); }
			for( Integer id : enemyFootmen) { enemyfootmanStartHealth += state.getUnit(id).getHP(); }
			initSize = myFootmen.size();
		}
		else { calcPreviousReward(state); }
		
		
		for (Integer ID : myFootmen)
		{
			Action attack = qLearning(state, ID, attacks);
			actions.put(ID, attack);
			healths.put(ID, state.getUnit(ID).getHP()); //update to current health
		}
		//adding this turn (a turnInstance) to the rewardsMap
		attackRewardsMap.get(episodeCount).add(new turnInstance(attacks, 0, myfootmanStartHealth, enemyfootmanStartHealth));
		
		turnCount++;
		
		return actions;
	}

	//Terminal Step
	public void terminalStep(StateView state) { 
		episodeCount += 1; //Counting the episodes
		totalTurns += turnCount;
		if (episodeCount % 10 == 0)
		{
			System.out.printf("Games Played\t Average Cumulative Reward\n%d \t\t%d\n", episodeCount, -1000 - (totalScore / totalTurns));
			totalTurns = 0;
		}
		turnCount = 0; //resetting the count of the turns (should start from zero for each episode)
	}
	
	//qLearning algorithm
	public Action qLearning(StateView state, Integer currentFootmenID, List<attackInstance> attacks) {
		List<Action> actionsList = getListOfPossibleActions(state, currentFootmenID);
		int modulus = -(episodeNum - episodeCount);
		int index;
		int footmanI = myFootmen.indexOf(currentFootmenID) + 1;
		if (footmanI > 5)
			index = enemyFootmen.size() - 1;
		else
			index = 0;
		Integer previousHealth = healths.get(currentFootmenID);
		int currentHealth = state.getUnit(currentFootmenID).getHP();
		int deltaHealth = 0;
		
		if(previousHealth != null)
			deltaHealth = currentHealth - previousHealth;
		if (turnCount > 0)
		{
			if (deltaHealth <  modulus)
				index = returnFarthest(currentFootmenID, state);
		}
		
		attacks.add(new attackInstance(currentFootmenID, enemyFootmen.get(index)));
		return actionsList.get(index);
		//return Action.createCompoundAttack(currentFootmenID, enemyFootmen.get(0)); //what you defaulted to originally
	}
	
	public int returnFarthest(Integer footmanID, StateView state){
		int furthestD = -100;
		int furthestID = -1;
		UnitView myUnit = state.getUnit(footmanID);
		int count = 0;
		
		for (Integer ID : enemyFootmen){
			UnitView enemyUnit = state.getUnit(ID);
			
			int distance = DistanceMetrics.chebyshevDistance(myUnit.getXPosition(), myUnit.getYPosition(), enemyUnit.getXPosition(), enemyUnit.getYPosition());
			
			if (distance > furthestD)
			{
				furthestD = distance;
				furthestID = count;
			}
			count++;
		}
		
		return furthestID;
	}
	
	//calculate the reward for the previous set of actions/attacks
	public void calcPreviousReward(StateView state)
	{
		turnInstance t;
		int mycurrentFootmenHealth = 0;
		int currentenemyFootmenHealth = 0;
		t = attackRewardsMap.get(episodeCount).get(attackRewardsMap.get(episodeCount).size()-1);
		for( Integer id : myFootmen) { mycurrentFootmenHealth += state.getUnit(id).getHP(); }
		for( Integer id : enemyFootmen) { currentenemyFootmenHealth += state.getUnit(id).getHP(); }
		
		if(attackRewardsMap.get(episodeCount).size() < 2)
		{
			t.reward += mycurrentFootmenHealth - myfootmanStartHealth;
			t.reward += enemyfootmanStartHealth - currentenemyFootmenHealth;
			t.reward -= (myfootmanStartCount - myFootmen.size()) * footmanDeathWorth;
			t.reward += (enemyfootmanStartCount - enemyFootmen.size()) * footmanDeathWorth;
		}
		else
		{
			t.reward += mycurrentFootmenHealth - attackRewardsMap.get(episodeCount).get(attackRewardsMap.get(episodeCount).size()-2).myTotalHealth;
			t.reward += currentenemyFootmenHealth - attackRewardsMap.get(episodeCount).get(attackRewardsMap.get(episodeCount).size()-2).enemyTotalHealth;
			t.reward -= (myfootmanStartCount - myFootmen.size()) * footmanDeathWorth;
			t.reward += (enemyfootmanStartCount - enemyFootmen.size()) * footmanDeathWorth;
		}
		totalScore += t.reward;
	}
	
	//returns a list of ALL possible actions for a specific friendly footmen
	public List<Action> getListOfPossibleActions(StateView state, Integer currentFootmenID) {
		List<Action> ListOfPossibleActions = new ArrayList<Action>();
		for(Integer enemyFootman : enemyFootmen) {
			ListOfPossibleActions.add(Action.createCompoundAttack(currentFootmenID, enemyFootman));
		}
		return ListOfPossibleActions;
	}
	
	//Prints out the health of the friendly footmen
	public void printHealth(StateView state, List<Integer> footmenList)
	{
		for(int i = 0; i < footmenList.size(); i++)
		{
			System.out.print(" " + state.getUnit(footmenList.get(i)).getHP());
		}
		System.out.println();
	}
	
	//Updates footmen lists
	public void updateFootmen(StateView state) {
		footmenIDs = findUnitType(state.getAllUnitIds(), state, footmen);
		myFootmen = new ArrayList<Integer>();
		enemyFootmen = new ArrayList<Integer>();
		
		for (Integer ID : footmenIDs) {
			if (state.getUnit(ID).getTemplateView().getPlayer() == playernum)
				myFootmen.add(ID);
			else
				enemyFootmen.add(ID);
		}
	}

	//returns the list of IDs in the world for the specific unit type.
	public List<Integer> findUnitType(List<Integer> ids, StateView state, String name) {
		List<Integer> unitIds = new ArrayList<Integer>();

		for (int x = 0; x < ids.size(); x++) {
			Integer unitId = ids.get(x);
			UnitView unit = state.getUnit(unitId);

			if (unit.getTemplateView().getUnitName().equals(name))
				unitIds.add(unitId);
		}
		return unitIds;
	}
	
	//Configuration XML setting - environment node
	private void configureEnvPrefs(Preferences prefs) throws BackingStoreException {
		Preferences envPrefs = prefs.node("environment");
		envPrefs.clear();
		envPrefs.putInt("NumEpisodes",episodeNum);		
	}
	//Configuration XML setting - model node
	private void configureModelPrefs(Preferences prefs) throws BackingStoreException {
		Preferences modelPrefs = prefs.node("model");
		modelPrefs.clear();
		modelPrefs.putBoolean("Conquest", true);
		modelPrefs.putBoolean("Midas", false);
		modelPrefs.putBoolean("ManifestDestiny", false);
		modelPrefs.putInt("TimeLimit", 65535);
	}
}