#EECS391 Programming Assignment 5

 - Developed By: Chien-Hung Chen and Christopher Gross
 - Case IDs: cxc330 and cjg28
 - Last update: 5/2/2012
 - Github Link: https://github.com/cxc330/EECS391_prog5

In this assignment we will use probabilistic reasoning to solve a scouting/resource collection scenario in the SimpleRTS game.

##1. How To Run
###Using Shell Scripts (The Easy Way)
We have included a few shell scripts for your pleasure.
Please run the following to run our agent:
	
	./run.sh
	
To clean the .class files up:
	
	./clean.sh

###Using Commands
To compile:
	
	javac -cp 'lib/SimpleRTSv3.1.jar'  src/edu/cwru/SimpleRTS/agent/ReinforcementAgent.java src/edu/cwru/SimpleRTS/agent/turnInstance.java src/edu/cwru/SimpleRTS/agent/attackInstance.java
	
	
To Run:
	
	java -cp lib/SimpleRTSv3.1.jar:src edu.cwru.SimpleRTS.Main --config data/defaultConfig.xml data/rl_5fv5f.map --agent edu.cwru.SimpleRTS.agent.ReinforcementAgent 0 --agentparam 50 --agent edu.cwru.SimpleRTS.agent.visual.VisualAgent 0 --agentparam true --agentparam true --agent  CombatAgent 1
	
Where the rl_5fv5f.map can be interchanged for another map (rl_10fv10f.map) and where the --agentparam 50 is the number of regular episodes you want to run (for every 10 of these, 5 evaluation episodes will also be ran so putting in a value of 50 runs 75 total episodes).

##2. Problem Setup

The scenario you will solve is built around the “rl_5fv5f.map” and the “rl_10fv10f.map” maps in the zip file and the  defaultConfig configuration file. In these map, there  are  5 or 10 “footmen” for each side. Footmen are melee units that attack with swords. Each footman has a fixed amount of “hitpoints.” When hit, it loses some hitpoints. If the current hitpoints reach zero, it dies. Your agent will control one side and the  provided CombatAgent will control the other side. Your reinforcement learning agent’s goal is (obviously) to learn a policy to win the battle---i.e., to kill the enemy footmen while losing as few footmen of its own as possible. Note that, unlike in planning, there is no separate “offline” component---the agent will learn by interacting directly with the environment and repeatedly playing the scenario. Also observe that, in this situation, an accurate model is not easy to specify ahead of time, so planning techniques are problematic to implement here.

The parameterized actions available to your agent will be of the form “Attack(F,E),” where F is a friendly footman and E is an enemy footman. At each  event point, you will loop through all living friendly footmen and reallocate targets to each one. An event is a “significant” state change, such as when a friendly unit gets hit or a target is killed. You are free to define your own set of events. Note that if you make the events too fine grained, you will have a lot of decision points with very long times between 
feedback (rewards), which will make the decision making problem much harder. On the other hand, if they are too coarse, your policy will be suboptimal because you will not react to changes quickly enough during the battle.

When an action Attack(F=f, E=e) has been selected for footman f, it must be executed in SimpleRTS. Note that such an action is a composite action that involves moving to e and then attacking e. You can use the built-in SimpleRTS compound attacks to handle this, but be careful of pathfinding issues in close quarters.

The reward function should be set up as follows. Each action costs the agent -0.1. If a friendly footman hits an enemy for d damage, it gets a reward of +d. If a friendly footman gets hit for d damage, it gets a penalty of –d. If an enemy footman dies,  your agent gets a reward of +100. If a friendly footman dies, it gets a penalty of -100.

###Q-learning with function approximation (100 points)

Implement the Q-learning algorithm with linear function approximation to solve this problem. The Q(s,a) function will be defined as w*f(s,a)+w0, where w is a vector of learned weights and f(s,a) is a vector of state-action features derived from the primitive state. For example, a feature might be: “Is e my closest enemy in terms of Chebyshev distance?” (Note that the enemy e is part of the action.) You should write your own set of features to use. Think about features that will help the agent to come up with good policies. Some useful features are “coordination” features such as “How many other footmen are currently attacking e?”. Some others are “reflective” features such as “Is e the enemy that is currently attacking f?” Yet other features could be things like “What is the ratio of the hitpoints of e to f?”

All of the friendly footmen will share the same Q-function. Thus this same function will be updated whenever any unit gets feedback, and will be consulted to determine the action of every unit. This is OK because all the units have identical capabilities and prevents combinatorial explosion due to multiple units. However, it also means that any “coordination” will have to implemented using state features. Note that each footman still senses the “global” state, and “knows” what the other friendly footmen are doing. This is an example of “central control.” Consult the book and the slides to see the update rules for learning the weights w. Note that in order to do the update properly, you need to “decompose” the rewards on a per-footman basis. Fortunately this is easy to do in this case as this is how I have specified the reward signal. (In general the reward signal would just be a function of the joint state and action.)

One other issue to note is that based on your event definitions, the time between successive decision points may vary. (You could of course also decide to have a “dummy  event”  always happen every 10 steps, say.) In this case you still need to track the accumulated reward over the intermediate time steps and discount them suitably when performing the Q-update. Use a discount factor of 0.9 for this problem, and a learning rate of 0.001, and perform epsilon-greedy exploration with epsilon set to 0.05. Start with the weights set to random initial values in (-1,1). 

Your agent should take one option, which is the number of episodes to play. Each episode is a complete battle up to a victory or defeat. Given this option, the agent should play the number of specified episodes against the opponent. Every 10 episodes, it should freeze its current policy (Q-function) and play another 5 “evaluation” episodes against the opponent (during which the Q function is used to select actions, but not updated). Then it should produce the following output:

|Games Played |	Average Cumulative Reward|
|------|------:|
|0  |	xyz|
|10 |	xyz|
|20 |	xyz|
|30 |	xyz|


Each “xyz” is the average (undiscounted) cumulative reward obtained by the current policy/Q-function after 0, 10, 20 etc. games played, averaged over the five evaluation games. This gives you an idea of the rate at which the agent is learning and can be plotted as a learning curve. mportant note: You can fix the PRNG seed for this agent right at the start to 12345 to ensure repeatability, however, be careful not to reset the seed at the start of each episode! It is important to let each episode play out differently for the policy to improve.
