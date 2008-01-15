package sernet.hui.common.rules;

import org.apache.log4j.Logger;

public abstract class RuleFactory {

	public static IFillRule getDefaultRule(String name) {
		IFillRule rule;
		try {
			rule = (IFillRule) Class.forName("sernet.hui.common.rules."+name).newInstance();
			return rule;
		} catch (InstantiationException e) {
			Logger.getLogger(RuleFactory.class).error("Klasse für angegebene Regel nicht gefunden: " + name);
		} catch (IllegalAccessException e) {
			Logger.getLogger(RuleFactory.class).error("Klasse für angegebene Regel nicht gefunden: " + name);
		} catch (ClassNotFoundException e) {
			Logger.getLogger(RuleFactory.class).error("Klasse für angegebene Regel nicht gefunden: " + name);
		}
		return new NullRule();
	}
}
