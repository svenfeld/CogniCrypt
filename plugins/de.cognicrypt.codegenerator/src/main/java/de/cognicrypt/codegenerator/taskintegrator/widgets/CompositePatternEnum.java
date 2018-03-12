package de.cognicrypt.codegenerator.taskintegrator.widgets;

import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import de.cognicrypt.codegenerator.Constants;
import de.cognicrypt.codegenerator.UIConstants;
import de.cognicrypt.codegenerator.taskintegrator.models.ClaferFeature;
import de.cognicrypt.codegenerator.taskintegrator.models.ClaferModel;

public class CompositePatternEnum extends CompositePattern {

	private boolean sortable;
	private ArrayList<CompositeSortableTextItem> sortableTextItems;

	/**
	 * instantiate the pattern composite with optional ordering functionality
	 * 
	 * @param parent
	 *        parent {@link Composite}
	 * @param sortable
	 *        <code>true</code> if the enumeration items should be sortable, <code>false</code> otherwise
	 */
	public CompositePatternEnum(Composite parent, boolean sortable) {
		super(parent);

		this.sortable = sortable;
		sortableTextItems = new ArrayList<>();

		Button btnAddOption = new Button(this, SWT.NONE);
		btnAddOption.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		btnAddOption.setText("Add option");
		btnAddOption.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				CompositeSortableTextItem compositeSortableTextItem = new CompositeSortableTextItem(compositeOptions, sortable);
				compositeSortableTextItem.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

				sortableTextItems.add(compositeSortableTextItem);

				compositeOptions.layout();
				compositeScrolledOptions.setMinSize(compositeOptions.computeSize(SWT.DEFAULT, SWT.DEFAULT));

				updatePatternItemWidgets();

				super.widgetSelected(e);
			}
		});

		compositeScrolledOptions = new ScrolledComposite(this, SWT.BORDER | SWT.V_SCROLL);
		compositeScrolledOptions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		compositeScrolledOptions.setLayout(new GridLayout(1, false));

		compositeOptions = new Composite(compositeScrolledOptions, SWT.NONE);
		compositeOptions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		compositeOptions.setLayout(new GridLayout(1, false));
		compositeScrolledOptions.setContent(compositeOptions);

		compositeScrolledOptions.setExpandHorizontal(true);
		compositeScrolledOptions.setExpandVertical(true);
		compositeScrolledOptions.setMinSize(compositeOptions.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private int getItemPosition(CompositeSortableTextItem needleItem) {
		int index = -1;

		for (int i = 0; i < sortableTextItems.size(); i++) {
			CompositeSortableTextItem refComposite = sortableTextItems.get(i);
			if (refComposite == needleItem) {
				index = i;
			}
		}

		return index;
	}

	private void swapTexts(int i, int j) {
		String tempString = sortableTextItems.get(i).getText();
		sortableTextItems.get(i).setText(sortableTextItems.get(j).getText());
		sortableTextItems.get(j).setText(tempString);
	}

	public void moveUp(CompositeSortableTextItem targetComposite) {

		int targetIndex = getItemPosition(targetComposite);

		if (targetIndex == -1 || targetIndex == 0) {
			return;
		}

		swapTexts(targetIndex, targetIndex - 1);
		updatePatternItemWidgets();
	}

	public void moveDown(CompositeSortableTextItem targetComposite) {

		int targetIndex = getItemPosition(targetComposite);

		if (targetIndex == -1 || targetIndex == sortableTextItems.size() - 1) {
			return;
		}

		swapTexts(targetIndex, targetIndex + 1);
		updatePatternItemWidgets();
	}

	public void remove(CompositeSortableTextItem targetComposite) {
		sortableTextItems.remove(targetComposite);
		targetComposite.dispose();

		updatePatternItemWidgets();

		compositeOptions.layout();
		compositeScrolledOptions.setMinSize(compositeOptions.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	/**
	 * update list positions and set the move buttons to enabled or disabled
	 */
	private void updatePatternItemWidgets() {
		for (int i = 0; i < sortableTextItems.size(); i++) {
			CompositeSortableTextItem item = sortableTextItems.get(i);
			item.setMoveButtonsEnabled();
			item.setPosition(i + 1);
		}
		
		if (sortableTextItems.size() == 1) {
			sortableTextItems.get(0).setMoveButtonsEnabled(false, false);
		} else if (sortableTextItems.size() > 1) {
			sortableTextItems.get(0).setMoveButtonsEnabled(false, true);
			sortableTextItems.get(sortableTextItems.size() - 1).setMoveButtonsEnabled(true, false);
		}
	}

	/**
	 * Get the result model depending on whether items in this enumeration are ordered or not. This will return a reference Clafer as a parent and the enumeration items as its
	 * children when the sortable field of the object set to <code>true</code>.
	 */
	public ClaferModel getResultModel() {
		ClaferModel resultModel = new ClaferModel();

		String parentFeatureInheritance;

		if (sortable) {
			parentFeatureInheritance = "Enum -> integer";
		} else {
			parentFeatureInheritance = "Enum";
		}
		resultModel.add(new ClaferFeature(Constants.FeatureType.ABSTRACT, patternName, parentFeatureInheritance));

		for (int i = 0; i < sortableTextItems.size(); i++) {
			String str = sortableTextItems.get(i).getText();
			StringBuilder childFeatureInheritance = new StringBuilder();

			childFeatureInheritance.append(patternName);

			if (sortable) {
				childFeatureInheritance.append(" = ");
				childFeatureInheritance.append(String.valueOf(i + 1));
			}

			resultModel.add(new ClaferFeature(Constants.FeatureType.CONCRETE, str.toString(), childFeatureInheritance.toString()));
		}

		return resultModel;
	}

	@Override
	public boolean validate() {
		boolean itemsValid = true;

		// check that items are unique
		HashSet<String> itemNames = new HashSet<>();

		for (CompositeSortableTextItem textItem : sortableTextItems) {
			// adding to the HashSet will return false if item already in set
			if (!textItem.getText().isEmpty() && !itemNames.add(textItem.getText())) {
				textItem.showValidationError(UIConstants.DEC_ERROR, "The name already exists.");
				itemsValid = false;
			} else {
				textItem.hideValidationError();
			}
		}

		return itemsValid && super.validate();
	}

}