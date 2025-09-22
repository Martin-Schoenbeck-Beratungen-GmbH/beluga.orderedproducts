package de.schoenbeck.beluga.orderedproducts;

import org.adempiere.base.IProcessFactory;
import org.compiere.process.ProcessCall;

public class OrderedProductsProcessFactory implements IProcessFactory{

	@Override
	public ProcessCall newProcessInstance(String className) {

		if (AddOrderlinesFromSelection.class.getCanonicalName().equals(className))
			return new AddOrderlinesFromSelection();
		
		return null;
	}

}
