package edu.cwru.SimpleRTS.agent;
import java.util.*;

//turnInstance class stores one turn in the episode and the list of ALL attacks that was assigned.
//It also stores the reward for doing this combination of attacks.
public class turnInstance {
	public List<attackInstance> listOfAttacks;
	public int reward;
	public int myTotalHealth;
	public int enemyTotalHealth;
	
	public turnInstance(List<attackInstance> attacks, int totalreward, int myTotalStartHealth, int enemyTotalStartHealth)
	{
		listOfAttacks = attacks;
		reward = totalreward;
		myTotalHealth = myTotalStartHealth;
		enemyTotalHealth = enemyTotalStartHealth;
	}
}
