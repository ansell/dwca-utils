/*
 * Copyright (c) 2016, Peter Ansell
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.ansell.dwca;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.jooq.lambda.Unchecked;

import com.github.ansell.concurrent.jparallel.JParallel;
import com.github.ansell.csv.stream.CSVStream;
import com.github.ansell.dwca.DarwinCoreCoreOrExtension.CoreOrExtension;

import javanet.staxutils.IndentingXMLStreamWriter;

/**
 * Represents an entire Darwin Core Archive metadata.xml file as parsed into
 * memory.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * @see <a href="http://rs.tdwg.org/dwc/terms/guides/text/">Darwin Core Text
 *      Guide</a>
 */
public class DarwinCoreArchiveDocument implements Iterable<DarwinCoreRecord> {

	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("DarwinCoreArchiveDocument [\n");
		builder.append("core=");
		builder.append(core);
		builder.append(", \n");
		builder.append("extensions=");
		builder.append(extensions != null ? extensions.subList(0, Math.min(extensions.size(), maxLen)) : null);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Writes out this archive document to XML.
	 * 
	 * @param out
	 *            The Writer to write the document to.
	 * @param showDefaults
	 *            True to show some default values, for increased
	 *            interoperability with buggy implementations.
	 * @throws XMLStreamException
	 *             If there is an XML related problem while writing the
	 *             document.
	 * @throws IOException
	 *             If there is an IO problem while writing the document.
	 * @throws IllegalStateException
	 *             If there is a semantic constraint violation while writing the
	 *             document.
	 */
	public void toXML(Writer out, boolean showDefaults) throws XMLStreamException, IOException, IllegalStateException {
		checkConstraints();

		XMLOutputFactory factory = XMLOutputFactory.newFactory();
		XMLStreamWriter writer = new IndentingXMLStreamWriter(factory.createXMLStreamWriter(out));
		writer.writeStartDocument();
		writer.writeStartElement(DarwinCoreArchiveConstants.ARCHIVE);
		writer.writeNamespace("", DarwinCoreArchiveConstants.DWC);
		writer.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		writer.writeNamespace("xs", "http://www.w3.org/2001/XMLSchema");
		writer.writeAttribute("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation",
				"http://rs.tdwg.org/dwc/text/ http://rs.tdwg.org/dwc/text/tdwg_dwc_text.xsd");
		core.toXML(writer, showDefaults);
		for (DarwinCoreCoreOrExtension extension : extensions) {
			extension.toXML(writer, showDefaults);
		}
		// end archive
		writer.writeEndElement();
		writer.writeEndDocument();
	}

	private DarwinCoreCoreOrExtension core;

	private final List<DarwinCoreCoreOrExtension> extensions = new ArrayList<>();

	/**
	 * The Path to the meta.xml file that this object represents, or null if it
	 * was created in memory.
	 */
	private Path metadataXMLPath;

	public Optional<Path> getMetadataXMLPath() {
		return Optional.ofNullable(metadataXMLPath);
	}

	public DarwinCoreCoreOrExtension getCore() {
		if (core == null) {
			throw new IllegalStateException("Could not find core in this document");
		}
		return core;
	}

	public void setCore(DarwinCoreCoreOrExtension core) {
		if (this.core != null) {
			throw new IllegalStateException("Multiple core elements found for darwin core archive document");
		}
		if (core.getType() != CoreOrExtension.CORE) {
			throw new IllegalStateException("The core must be typed as core");
		}
		this.core = core;
	}

	public List<DarwinCoreCoreOrExtension> getExtensions() {
		return Collections.unmodifiableList(extensions);
	}

	public void addExtension(DarwinCoreCoreOrExtension extension) {
		if (extension.getType() != CoreOrExtension.EXTENSION) {
			throw new IllegalStateException("All extensions must be typed as extension.");
		}
		this.extensions.add(extension);
	}

	/**
	 * Checks that semantic constraints are enforced and throws an exception if
	 * they are found not to be enforced.
	 * 
	 * @throws IllegalStateException
	 *             If semantic constraints are not enforced.
	 */
	public void checkConstraints() throws IllegalStateException {
		if (core.getFields().isEmpty()) {
			throw new IllegalStateException("Core must have fields.");
		}
		if (core.getFiles() == null) {
			throw new IllegalStateException("Core must have files set.");
		}
		for (DarwinCoreField field : core.getFields()) {
			if (field.getTerm() == null) {
				throw new IllegalStateException("All fields must have term set");
			}
			if (field.getIndex() == null && field.getDefault() == null) {
				throw new IllegalStateException(
						"Fields that do not have indexes must have default values set: " + field.getTerm());
			}
		}

		if (!this.getExtensions().isEmpty() && this.getCore().getIdOrCoreId() == null) {
			throw new IllegalStateException("Core must have id set if there are extensions present.");
		}

		for (DarwinCoreCoreOrExtension extension : getExtensions()) {
			if (extension.getIdOrCoreId() == null) {
				throw new IllegalStateException("Extensions must have coreId set.");
			}
			if (extension.getFields().isEmpty()) {
				throw new IllegalStateException("Extension must have fields.");
			}
			if (extension.getFiles() == null) {
				throw new IllegalStateException("Extension must have files set.");
			}
			for (DarwinCoreField field : extension.getFields()) {
				if (field.getTerm() == null) {
					throw new IllegalStateException("All extension fields must have term set");
				}
				if (field.getIndex() == null && field.getDefault() == null) {
					throw new IllegalStateException(
							"Fields that do not have indexes must have default values set: " + field.getTerm());
				}
			}
		}
	}

	@Override
	public CloseableIterator<DarwinCoreRecord> iterator() {
		DarwinCoreRecord sentinel = (DarwinCoreRecord) new Object();
		BlockingQueue<DarwinCoreRecord> pendingResults = new ArrayBlockingQueue<>(10);
		DarwinCoreArchiveDocument document = this;
		JParallel<List<String>, DarwinCoreRecord> parallelBuilder = JParallel
				.forFunctions(line -> new DarwinCoreRecord() {

					@Override
					public List<String> getValues() {
						// FIXME: Merge the other files fields together
						return line;
					}

					@Override
					public List<DarwinCoreField> getFields() {
						// FIXME: Merge the other fields together
						return getDocument().getCore().getFields();
					}

					@Override
					public DarwinCoreArchiveDocument getDocument() {
						return document;
					}
				}, record -> pendingResults.offer(record));

		JParallel<List<String>, DarwinCoreRecord> startedParallel = parallelBuilder.inputProcessors(1)
				.outputConsumers(1).start();

		// Create a parse function
		final Consumer<Reader> parseFunction = DarwinCoreArchiveChecker.createParseFunction(core, h -> {
		}, (h, l) -> l, l -> startedParallel.add(l));

		return new CloseableIterator<DarwinCoreRecord>() {

			private final AtomicBoolean started = new AtomicBoolean(false);
			private final CountDownLatch startCompleted = new CountDownLatch(1);
			private final AtomicBoolean closed = new AtomicBoolean(false);
			private volatile DarwinCoreRecord nextItem;
			private final ExecutorService executor = Executors.newFixedThreadPool(1);
			private final AtomicReference<Future<?>> runningJob = new AtomicReference<>();

			private void checkStart() {
				if (started.compareAndSet(false, true)) {
					try {
						runningJob.getAndSet(executor.submit(Unchecked
								.runnable(() -> DarwinCoreArchiveChecker.parseCoreOrExtension(document.getCore(),
										document.getMetadataXMLPath().orElse(null), parseFunction))));
					} finally {
						startCompleted.countDown();
					}
				}
			}

			@Override
			public void close() {
				if (closed.compareAndSet(false, true)) {
					try {
						checkStart();
						startCompleted.await();
						while (!pendingResults.offer(sentinel, 10, TimeUnit.SECONDS)) {
							pendingResults.clear();
						}
						Future<?> future = runningJob.get();
						if (future != null && !future.isDone()) {
							future.cancel(true);
						}
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						e.printStackTrace();
					} finally {
						startedParallel.close();
					}
				}
			}

			@Override
			public DarwinCoreRecord next() {
				if (!closed.get()) {
					try {
						checkStart();
						startCompleted.await();
						return nextItem;
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
				throw new NoSuchElementException("No other records found");
			}

			@Override
			public boolean hasNext() {
				try {
					if (closed.get()) {
						return false;
					}
					if (Thread.currentThread().isInterrupted()) {
						close();
						return false;
					}
					checkStart();
					startCompleted.await();
					DarwinCoreRecord poll = pendingResults.take();
					if (poll == sentinel) {
						close();
						return false;
					} else {
						nextItem = poll;
						return true;
					}
				} catch (InterruptedException e) {
					close();
					Thread.currentThread().interrupt();
					return false;
				}
			}
		};
	}

}
