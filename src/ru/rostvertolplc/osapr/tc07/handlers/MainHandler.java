package ru.rostvertolplc.osapr.tc07.handlers;

import java.util.Vector;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.AIFClipboard;
import com.teamcenter.rac.aif.AIFPortal;
import com.teamcenter.rac.aif.AIFTransferable;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class MainHandler extends AbstractHandler {

	private Vector<TCComponent> localVector;

	/**
	 * The constructor.
	 */
	public MainHandler() {
	}

	private void AddToClipboard(TCComponentItemRevision rev1)
			throws TCException {
		if (rev1 != null) {
			AIFComponentContext[] where = rev1.whereReferenced();
			for (AIFComponentContext cont1 : where) {
				if ((cont1.getComponent().toString().startsWith("TP_"
						+ rev1.getItem().getProperty("current_id")))
						&& (!localVector.contains((TCComponent) cont1
								.getComponent())))
					localVector.addElement((TCComponent) cont1.getComponent());
			}
		}
	}

	boolean CheckNode(TCComponentBOMLine bomLine1) {

		try {
			AddToClipboard(bomLine1.getItemRevision());
			AIFComponentContext[] arrayOfAIFComponentContext = null;
			try {
				arrayOfAIFComponentContext = bomLine1.getChildren();
			} catch (TCException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (arrayOfAIFComponentContext != null) {
				int i = arrayOfAIFComponentContext.length;
				TCComponentBOMLine bomLine2;
				for (int j = 0; j < i; j++) {
					bomLine2 = (TCComponentBOMLine) arrayOfAIFComponentContext[j]
							.getComponent();
					AddToClipboard(bomLine2.getItemRevision());
					if (bomLine2.hasChildren()) {
						if (!CheckNode(bomLine2))
							return false;
					}
				}
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		localVector = new Vector<TCComponent>();
		AbstractAIFUIApplication currentApplication = AIFUtility
				.getCurrentApplication();
		TCComponentBOMLine localTCComponentBOMLine;
		AIFComponentContext context1 = currentApplication.getTargetContext();
		if (context1 == null) {
			MessageBox.post("Сборка не выбрана!", "Teamcenter Error",
					MessageBox.ERROR);
			return null;
		}

		try {
			localTCComponentBOMLine = (TCComponentBOMLine) context1
					.getComponent();
		} catch (ClassCastException localClassCastException) {
			MessageBox.post(
					"Выбранный объект не является элементом структуры изделия",
					"Teamcenter Error", MessageBox.ERROR);
			return null;
		}

		CheckNode(localTCComponentBOMLine);

		if (!localVector.isEmpty()) {
			AIFClipboard localAIFClipboard = AIFPortal.getClipboard();
			AIFTransferable localAIFTransferable = new AIFTransferable(
					localVector);
			localAIFClipboard.setContents(localAIFTransferable, null);
			MessageBox.post("В буфер обмена скопировано объектов ТП: "
					+ Integer.toString(localVector.size()), "Teamcenter",
					MessageBox.INFORMATION);
		} else
			MessageBox.post("объектов ТП в сборке не обнаружено", "Teamcenter",
					MessageBox.INFORMATION);

		return null;
	}
}