package de.schoenbeck.beluga.orderedproducts;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProcessPara;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;

public class AddOrderlinesFromSelection extends SvrProcess {

	MOrder record = null;
	
	@Override
	protected void prepare() {
		
		for (var p : getParameter()) {
			switch (p.getParameterName()) {
			case "C_Order_ID": this.record = new MOrder(getCtx(), p.getParameterAsInt(), get_TrxName()); break;
			default:
				MProcessPara.validateUnknownParameter(getProcessInfo().getAD_Process_ID(), p);
			}
		}
	}

	@Override
	protected String doIt() throws Exception {
		
		if (this.record == null)
			throw new Exception("Record not detected");
		
		for (var p : getSelection()) {
			var line = new MOrderLine(record);
			line.setM_Product_ID(p.m_product_id);
			line.setQty(p.qty);
			line.setPrice();
			line.setPrice(p.price);
			line.saveEx();
		}
		
		return "@Success@"; //TODO: return amt created
	}
	
	ArrayList<Selection> getSelection() throws Exception {
		
		ArrayList<Selection> rtn = new ArrayList<>();
		String sql = """
				SELECT *, COALESCE(Value_Number::Text, Value_String, Value_Date::Text) as Value_Coalesced
				FROM T_Selection_InfoWindow
				WHERE AD_PInstance_ID=?
				ORDER BY T_Selection_ID
				""";
		PreparedStatement ps = DB.prepareStatement(sql, get_TrxName());
		ps.setInt(1, getAD_PInstance_ID());
		ResultSet rs = null;
		try {
			rs = ps.executeQuery();
			
			int last_selection_id = 0;
			Selection sel = null;
			
			while (rs.next()) {
				if (last_selection_id != rs.getInt("T_Selection_ID")) {
					if (sel != null)
						rtn.add(sel);
					sel = new Selection();
					last_selection_id = rs.getInt("T_Selection_ID");
				}
				
				switch (rs.getString("ColumnName")) {
				case "M_Product_ID": sel.m_product_id = rs.getInt("Value_Number"); break;
				case "QtyOrdered": sel.qty = rs.getBigDecimal("Value_Number"); break;
				case "PriceEntered": sel.price = rs.getBigDecimal("Value_Number"); break;
				}
			}
			
			if (sel != null)
				rtn.add(sel);
		} finally {
			DB.close(rs, ps);
		}
		
		return rtn;
	}
	
	private class Selection {
		int m_product_id;
		BigDecimal qty;
		BigDecimal price;
	};
}
