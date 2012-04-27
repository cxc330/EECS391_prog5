package edu.cwru.SimpleRTS.agent;

import java.util.*;

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

	// Constructor
	public ReinforcementAgent(int playernum, String[] args) {
		super(playernum);

	}

	// Below is the initialStep and setup for our agent

	public Map<Integer, Action> initialStep(StateView state) {

		return middleStep(state);
	}

	// Below is the middleStep function where the majority of the logic works
	public Map<Integer, Action> middleStep(StateView state) {

		Map<Integer, Action> actions = new HashMap<Integer, Action>();
		updateFootmen(state);

		for (Integer ID : myFootmen) {

			Action attack = Action
					.createCompoundAttack(ID, enemyFootmen.get(0));

			actions.put(ID, attack);
		}
		return actions;
	}

	public void terminalStep(StateView state) {
	}

	/*
	 * Updates footmen lists
	 */
	public void updateFootmen(StateView state) {
		footmenIDs = findUnitType(state.getAllUnitIds(), state, footmen);
		myFootmen = new ArrayList<Integer>();
		enemyFootmen = new ArrayList<Integer>();

		for (Integer ID : footmenIDs) {
			UnitView footman = state.getUnit(ID);

			int playerNum = footman.getTemplateView().getPlayer();

			if (playerNum == playernum)
				myFootmen.add(ID);
			else
				enemyFootmen.add(ID);

		}
	}

	public List<Integer> findUnitType(List<Integer> ids, StateView state,
			String name) {

		List<Integer> unitIds = new ArrayList<Integer>();

		for (int x = 0; x < ids.size(); x++) {
			Integer unitId = ids.get(x);
			UnitView unit = state.getUnit(unitId);

			if (unit.getTemplateView().getUnitName().equals(name)) {
				unitIds.add(unitId);
			}
		}

		return unitIds;
	}
}
