/********************************************************************************
 * Copyright (c) 2015-2018 TU Darmstadt
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

/**
 *
 */
package de.cognicrypt.integrator.task.controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipFile;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import org.clafer.ast.AstClafer;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.cognicrypt.codegenerator.question.Page;
import de.cognicrypt.codegenerator.question.Question;
import de.cognicrypt.codegenerator.tasks.Task;
import de.cognicrypt.core.Constants;
import de.cognicrypt.integrator.task.Activator;
import de.cognicrypt.integrator.task.models.ClaferModel;
import de.cognicrypt.utils.Utils;

public class FileUtilities {

	private String taskName;
	private StringBuilder errors; // Maintain all the errors to display them on the wizard.

	/**
	 * The class needs to be initialized with a task name, as it is used extensively in the methods.
	 *
	 * @param taskName
	 */
	public FileUtilities(final String taskName) {
		super();
		setTaskName(taskName);
		setErrors(new StringBuilder());
	}

	/**
	 *
	 * @return the result of the comilation.
	 */
	private boolean compileCFRFile() {
		// try to compile the Clafer file
		final String claferFilename = Utils.getResourceFromWithin(Constants.CFR_FILE_DIRECTORY_PATH)
			.getAbsolutePath() + Constants.innerFileSeparator + getTrimmedTaskName() + Constants.CFR_EXTENSION;
		return ClaferModel.compile(claferFilename);
	}

	/**
	 * Write the data from the pages to target location in the plugin.
	 *
	 * @param claferModel
	 * @param questions
	 * @param xslFileContents
	 * @param customLibLocation
	 * @throws TransformerException
	 */
	public String writeFiles(final ClaferModel claferModel, final ArrayList<Question> questions, final String xslFileContents, final File customLibLocation, final String helpFileContents) {
		writeXSLFile(xslFileContents);

		if (helpFileContents != null) {
			writeHelpFile(helpFileContents);
		}
		// custom library location is optional.
		if (customLibLocation != null) {
			if (validateJARFile(customLibLocation)) {
				copyFileFromPath(customLibLocation);
			}
		}
		if (getErrors().length() > 0) {
			return getErrors().toString();
		}
		writeCFRFile(claferModel);
		compileCFRFile();
		writeJSONFile(questions);
		return getErrors().toString();
	}

	private void writeHelpFile(final String helpFileContents) {
		final File xmlFile = new File(Utils.getResourceFromWithin(Constants.HELP_FILE_DIRECTORY_PATH), getTrimmedTaskName() + Constants.XML_EXTENSION);

		try {
			final PrintWriter writer = new PrintWriter(xmlFile);
			writer.println(helpFileContents);
			writer.flush();
			writer.close();
		} catch (final FileNotFoundException e) {
			Activator.getDefault().logError(e);
			getErrors().append("There was a problem wrting the Help data.\n");
		}

		if (!validateXMLFile(xmlFile)) {
			xmlFile.delete();
			getErrors().append("The XML data is invalid.\n");
		}

	}

	/**
	 * Copy the selected files to target location in the plugin.
	 *
	 * @param cfrFileLocation
	 * @param jsonFileLocation
	 * @param xslFileLocation
	 * @param customLibLocation
	 * @param file
	 */
	public String writeFiles(final File cfrFileLocation, final File jsonFileLocation, final File xslFileLocation, final File customLibLocation, final File helpLocation) {

		final boolean isCFRFileValid = validateCFRFile(cfrFileLocation);
		final boolean isJSONFileValid = validateJSONFile(jsonFileLocation);
		final boolean isXSLFileValid = validateXSLFile(xslFileLocation);

		if (isCFRFileValid && isJSONFileValid && isXSLFileValid) {

			// custom library location is optional.
			if (customLibLocation != null) {
				if (validateJARFile(customLibLocation)) {
					copyFileFromPath(customLibLocation);
				}
			}
			if (helpLocation != null) {
				if (validateXMLFile(helpLocation)) {
					copyFileFromPath(helpLocation);
				}
			}

			// help location of optional.

			copyFileFromPath(cfrFileLocation);
			copyFileFromPath(jsonFileLocation);

			final String cfrFilename = cfrFileLocation.getAbsolutePath();
			final String jsFilename = cfrFilename.substring(0, cfrFilename.lastIndexOf(".")) + Constants.JS_EXTENSION;
			copyFileFromPath(new File(jsFilename));

			copyFileFromPath(xslFileLocation);

		}

		return getErrors().toString();
	}

	/**
	 * Validate an XML file.
	 *
	 * @param helpLocation
	 * @return
	 */
	private boolean validateXMLFile(final File helpLocation) {
		final SAXReader reader = new SAXReader();
		reader.setValidation(false);
		try {
			reader.read(helpLocation);
		} catch (final DocumentException e) {
			Activator.getDefault().logError(e);
			appendFileErrors(helpLocation.getName());
			return false;
		}
		return true;
	}

	/**
	 * For the sake of reusability.
	 *
	 * @param fileName
	 */
	private void appendFileErrors(final String fileName) {
		getErrors().append("The contents of the file ");
		getErrors().append(fileName);
		getErrors().append(" are invalid.");
		getErrors().append("\n");
	}

	/**
	 * Validate the provided JAR file before copying it to the target location.
	 *
	 * @param customLibLocation
	 * @return a boolean value for the validity of the file.
	 */
	private boolean validateJARFile(final File customLibLocation) {
		final boolean validFile = true;
		// Loop through the files, since the custom library is a directory.
		if (customLibLocation.isDirectory()) {
			for (final File tmpLibLocation : customLibLocation.listFiles()) {
				if (tmpLibLocation.getPath().endsWith(Constants.JAR_EXTENSION)) {
					ZipFile customLib;
					try {
						customLib = new ZipFile(tmpLibLocation);
						customLib.entries();
						customLib.close();
					} catch (final IOException ex) {
						Activator.getDefault().logError(ex);
						appendFileErrors(tmpLibLocation.getName());
						return false;
					}
				}
			}
		}
		return validFile;
	}

	/**
	 * Validate the provided XSL file before copying it to the target location.
	 *
	 * @param xslFileLocation
	 * @return a boolean value for the validity of the file.
	 */
	private boolean validateXSLFile(final File xslFileLocation) {
		try {
			TransformerFactory.newInstance().newTransformer(new StreamSource(xslFileLocation));
		} catch (final TransformerConfigurationException e) {
			Activator.getDefault().logError(e);
			appendFileErrors(xslFileLocation.getName());
			return false;
		}
		return true;
	}

	/**
	 * Validate the provided JSON file before copying it to the target location.
	 *
	 * @param jsonFileLocation
	 * @return a boolean value for the validity of the file.
	 */
	private boolean validateJSONFile(final File jsonFileLocation) {
		try {
			final Gson gson = new Gson();
			final BufferedReader reader = new BufferedReader(new FileReader(jsonFileLocation));
			gson.fromJson(reader, Object.class);
			reader.close();
			return true;
		} catch (IOException e) {
			Activator.getDefault().logError(e);
			appendFileErrors(jsonFileLocation.getName());
			return false;
		}
	}

	/**
	 * Validate the provided CFR file before copying it to the target location.
	 *
	 * @param cfrFileLocation
	 * @return a boolean value for the validity of the file.
	 */
	private boolean validateCFRFile(final File cfrFileLocation) {
		final boolean compilationResult = ClaferModel.compile(cfrFileLocation.getAbsolutePath());
		final String cfrFilename = cfrFileLocation.getAbsolutePath();
		final String jsFilename = cfrFilename.substring(0, cfrFilename.lastIndexOf(".")) + Constants.JS_EXTENSION;
		final de.cognicrypt.codegenerator.featuremodel.clafer.ClaferModel claferModel = new de.cognicrypt.codegenerator.featuremodel.clafer.ClaferModel(jsFilename);
		final AstClafer taskClafer = org.clafer.cli.Utils.getModelChildByName(claferModel.getModel(), "c0_" + getTrimmedTaskName());
		final boolean isValidationSuccessful = compilationResult && taskClafer != null;
		if (!isValidationSuccessful) {
			appendFileErrors(cfrFileLocation.getName());
			getErrors()
				.append("Either the compilation failed, or the provided name for the Task does not match the one in the Clafer model. Please note : the name must be capitalized.");
			getErrors().append("\n");
		}
		return isValidationSuccessful;
	}

	/**
	 * Copy the given file to the appropriate location.
	 *
	 * @param existingFileLocation
	 */
	private void copyFileFromPath(final File existingFileLocation) {
		if (existingFileLocation.exists() && !existingFileLocation.isDirectory()) {
			File targetDirectory = null;
			try {

				if (existingFileLocation.getPath().endsWith(Constants.CFR_EXTENSION)) {
					targetDirectory = new File(Utils.getResourceFromWithin(Constants.CFR_FILE_DIRECTORY_PATH), getTrimmedTaskName() + Constants.CFR_EXTENSION);
				} else if (existingFileLocation.getPath().endsWith(Constants.JS_EXTENSION)) {
					targetDirectory = new File(Utils.getResourceFromWithin(Constants.CFR_FILE_DIRECTORY_PATH), getTrimmedTaskName() + Constants.JS_EXTENSION);
				} else if (existingFileLocation.getPath().endsWith(Constants.JSON_EXTENSION)) {
					targetDirectory = new File(Utils.getResourceFromWithin(Constants.JSON_FILE_DIRECTORY_PATH), getTrimmedTaskName() + Constants.JSON_EXTENSION);
				} else if (existingFileLocation.getPath().endsWith(Constants.XSL_EXTENSION)) {
					targetDirectory = new File(Utils.getResourceFromWithin(Constants.XSL_FILE_DIRECTORY_PATH), getTrimmedTaskName() + Constants.XSL_EXTENSION);
				} else if (existingFileLocation.getPath().endsWith(Constants.XML_EXTENSION)) {
					targetDirectory = new File(Utils.getResourceFromWithin(Constants.HELP_FILE_DIRECTORY_PATH), getTrimmedTaskName() + Constants.XML_EXTENSION);
				} else {
					throw new Exception("Unknown file type.");
				}

				if (targetDirectory != null) {
					Files.copy(existingFileLocation.toPath(), targetDirectory.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
				}

			} catch (final Exception e) {
				Activator.getDefault().logError(e);
				getErrors().append("There was a problem copying file ");
				getErrors().append(existingFileLocation.getName());
				getErrors().append("\n");
			}
			// If we are dealing with a custom library location.
		} else if (existingFileLocation.exists() && existingFileLocation.isDirectory()) {
			final File tempDirectory = new File(Utils.getResourceFromWithin(Constants.JAR_FILE_DIRECTORY_PATH), getTrimmedTaskName() + Constants.innerFileSeparator);
			tempDirectory.mkdir();
			// Loop through all the containing files.
			for (final File customLibFile : existingFileLocation.listFiles()) {
				final File tmpFile = new File(tempDirectory.toString() + Constants.innerFileSeparator + customLibFile.getName());
				try {
					Files.copy(customLibFile.toPath(), tmpFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
				} catch (final IOException e) {
					Activator.getDefault().logError(e);
					getErrors().append("There was a problem copying file ");
					getErrors().append(existingFileLocation.getName());
					getErrors().append("\n");
				}
			}
		}
	}

	/**
	 * Update the task.json file with the new Task.
	 *
	 * @param task
	 *        the Task to be added.
	 */
	public void writeTaskToJSONFile(final Task task) {

		BufferedReader reader = null;
		BufferedWriter writer = null;
		final Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			reader = new BufferedReader(new FileReader(Utils.getResourceFromWithin(Constants.jsonTaskFile)));
			final List<Task> tasks = gson.fromJson(reader, new TypeToken<List<Task>>() {}.getType());
			// Add the new task to the list.
			tasks.add(task);
			reader.close();

			writer = new BufferedWriter(new FileWriter(Utils.getResourceFromWithin(Constants.jsonTaskFile)));
			gson.toJson(tasks, new TypeToken<List<Task>>() {}.getType(), writer);
			writer.close();

		} catch (final IOException e) {
			Activator.getDefault().logError(e);
			getErrors().append("There was a problem updating the task file.\n");
		}
	}

	/**
	 *
	 * @param claferModel
	 */
	private void writeCFRFile(final ClaferModel claferModel) {
		final File cfrFile = new File(Utils.getResourceFromWithin(Constants.CFR_FILE_DIRECTORY_PATH), getTrimmedTaskName() + Constants.CFR_EXTENSION);
		try {
			final FileWriter writer = new FileWriter(cfrFile);
			writer.write(claferModel.toString());
			writer.close();
		} catch (final IOException e) {
			Activator.getDefault().logError(e);
			getErrors().append("There was a problem writing the Clafer model.\n");
		}
	}

	/**
	 *
	 * @param questions
	 *        listOfAllQuestions
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	private void writeJSONFile(final ArrayList<Question> questions) {

		final SegregatesQuestionsIntoPages pageContent = new SegregatesQuestionsIntoPages(questions);
		final ArrayList<Page> pages = pageContent.getPages();
		boolean taskHasPageHelpContent = false;
		for (final Page page : pages) {
			for (final Question question : page.getContent()) {
				if (!question.getHelpText().isEmpty()) {
					taskHasPageHelpContent = true;
					break;
				}
			}
		}

		/**
		 * creates the xml file containing the help content of the task, adds the location of the xml file in the plugin.xml file and sets the page help id
		 */
		try {
			new CreateAndModifyXmlfile(pages, getTaskName(), taskHasPageHelpContent);
		} catch (IOException | ParserConfigurationException | SAXException | TransformerException e) {
			e.printStackTrace();
		}

		final File jsonFile = new File(Utils.getResourceFromWithin(Constants.JSON_FILE_DIRECTORY_PATH), getTaskName() + Constants.JSON_EXTENSION);

		try {
			final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
			//creates the file
			jsonFile.createNewFile();

			//creates the writer object for json file
			final FileWriter writerForJsonFile = new FileWriter(jsonFile);

			//write the data into the .json file
			writerForJsonFile.write(gson.toJson(pages));
			writerForJsonFile.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		if (!validateJSONFile(jsonFile)) {
			jsonFile.delete();
		}
	}

	/**
	 *
	 * @param xslFileContents
	 */
	private void writeXSLFile(final String xslFileContents) {
		final File xslFile = new File(Utils.getResourceFromWithin(Constants.XSL_FILE_DIRECTORY_PATH), getTrimmedTaskName() + Constants.XSL_EXTENSION);

		try {
			final PrintWriter writer = new PrintWriter(xslFile);
			writer.println(xslFileContents);
			writer.flush();
			writer.close();
		} catch (final FileNotFoundException e) {
			Activator.getDefault().logError(e);
			getErrors().append("There was a problem wrting the XSL data.\n");
		}

		if (!validateXSLFile(xslFile)) {
			xslFile.delete();
			getErrors().append("The XSL data is invalid.\n");
		}
	}

	public void updateThePluginXMLFileWithHelpData(final String machineReadableTaskName) {
		File pluginXMLFile = Utils.getResourceFromWithin(Constants.PLUGIN_XML_FILE);
		if (!pluginXMLFile.exists()) {
			pluginXMLFile = Utils.getResourceFromWithin("src" + Constants.innerFileSeparator + ".." + Constants.innerFileSeparator + Constants.PLUGIN_XML_FILE);
		}
		final SAXReader reader = new SAXReader();
		Document pluginXMLDocument = null;
		reader.setValidation(false);
		try {
			pluginXMLDocument = reader.read(pluginXMLFile);
		} catch (final DocumentException e) {
			Activator.getDefault().logError(e);
		}
		if (pluginXMLDocument != null) {

			final Element root = pluginXMLDocument.getRootElement();
			for (final Iterator<Element> extensionElement = root.elementIterator("extension"); extensionElement.hasNext();) {
				final Element currentExtensionElement = extensionElement.next();
				final Attribute point = currentExtensionElement.attribute("point");
				if (point != null && point.getValue().equals("org.eclipse.help.contexts")) {
					currentExtensionElement.addElement("contexts").addAttribute("file", Constants.HELP_FILE_DIRECTORY_PATH + machineReadableTaskName + Constants.XML_EXTENSION);
				}
			}

			try (FileWriter fileWriter = new FileWriter(pluginXMLFile)) {
				final OutputFormat format = OutputFormat.createPrettyPrint();
				final XMLWriter writer = new XMLWriter(fileWriter, format);
				writer.write(pluginXMLDocument);
				writer.close();
			} catch (final IOException e) {
				Activator.getDefault().logError(e);
			}
		}
	}

	/**
	 * Return the name of that task that is set for the file writes..
	 *
	 * @return
	 */
	private String getTaskName() {
		return this.taskName;
	}

	/**
	 * get machine-readable task name
	 *
	 * @return task name without non-alphanumerics
	 */
	private String getTrimmedTaskName() {
		return getTaskName().replaceAll("[^A-Za-z0-9]", "");
	}

	/**
	 *
	 * Set the name of the task that is being written to File. The names of the result files are set based on the provided task name.
	 *
	 * @param taskName
	 */
	private void setTaskName(final String taskName) {
		this.taskName = taskName;
	}

	/**
	 * @return the list of errors.
	 */
	private StringBuilder getErrors() {
		return this.errors;
	}

	/**
	 * @param set
	 *        the string builder to maintain the list of errors.
	 */
	private void setErrors(final StringBuilder errors) {
		this.errors = errors;
	}

}
