javac -cp 'lib/SimpleRTSv3.1.jar'  src/edu/cwru/SimpleRTS/agent/ReinforcementAgent.java

java -cp lib/SimpleRTSv3.1.jar:src edu.cwru.SimpleRTS.Main --config data/defaultConfig.xml data/rl_5fv5f.map --agent edu.cwru.SimpleRTS.agent.ReinforcementAgent 0 --agent edu.cwru.SimpleRTS.agent.visual.VisualAgent 0 --agentparam true --agentparam true --agent  CombatAgent 1