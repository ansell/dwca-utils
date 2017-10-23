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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
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
import java.util.function.BiFunction;
import java.util.function.Consumer;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.jooq.lambda.Unchecked;

import com.github.ansell.concurrent.jparallel.JParallel;
import com.github.ansell.dwca.DarwinCoreCoreOrExtension.CoreOrExtension;
import com.github.ansell.jdefaultdict.JDefaultDict;

import javanet.staxutils.IndentingXMLStreamWriter;

/**
 * Represents an entire Darwin Core Archive metadata.xml file as parsed into
 * memory.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * @see <a href="http://rs.tdwg.org/dwc/terms/guides/text/">Darwin Core Text
 *      Guide</a>
 */
public class DarwinCoreArchiveDocument implements Iterable<List<DarwinCoreRecord>>, ConstraintChecked {

	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("DarwinCoreArchiveDocument [\n");
		builder.append("metadataXMLPath=");
		builder.append(metadataXMLPath);
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
				DarwinCoreArchiveConstants.DWC + " http://rs.tdwg.org/dwc/text/tdwg_dwc_text.xsd");
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

	public void setMetadataXMLPath(Path metadataXMLPath) {
		this.metadataXMLPath = Objects.requireNonNull(metadataXMLPath, "Metadata XML Path cannot be set to null");
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
	@Override
	public void checkConstraints() throws IllegalStateException {
		core.checkConstraints();

		if (!this.getExtensions().isEmpty() && this.getCore().getIdOrCoreId() == null) {
			throw new IllegalStateException("Core must have id set if there are extensions present.");
		}

		for (DarwinCoreCoreOrExtension extension : getExtensions()) {
			extension.checkConstraints();
		}
	}

	@Override
	public CloseableIterator<List<DarwinCoreRecord>> iterator() {
		return iterator(true);
	}

	public CloseableIterator<List<DarwinCoreRecord>> iterator(boolean includeDefaults) {
		final DarwinCoreArchiveDocument document = this;
		// Dummy sentinel to signal when iteration is complete
		final DarwinCoreRecord sentinel = new DarwinCoreRecord() {
			@Override
			public DarwinCoreArchiveDocument getDocument() {
				return null;
			}

			@Override
			public DarwinCoreCoreOrExtension getCoreOrExtension() {
				return null;
			}

			@Override
			public Optional<String> valueFor(String term, boolean includeDefaults) {
				return Optional.empty();
			}
		};

		// Core helpers
		final BlockingQueue<DarwinCoreRecord> pendingResults = new ArrayBlockingQueue<>(1);
		// Create a parse function
		final BiFunction<List<String>, List<String>, DarwinCoreRecord> coreLineConverter = getLineConverter(document,
				document.getCore());
		final Consumer<DarwinCoreRecord> coreResultConsumer = getResultConsumer(pendingResults);
		final Consumer<Reader> parseFunction = DarwinCoreArchiveChecker.createParseFunction(core, h -> {
		}, coreLineConverter, coreResultConsumer, includeDefaults);

		// And the equivalent extension helpers
		final List<DarwinCoreCoreOrExtension> nextExtensions = getExtensions();
		final JDefaultDict<DarwinCoreCoreOrExtension, BlockingQueue<DarwinCoreRecord>> extensionQueues = new JDefaultDict<>(
				k -> new ArrayBlockingQueue<>(1));
		final JDefaultDict<DarwinCoreCoreOrExtension, BiFunction<List<String>, List<String>, DarwinCoreRecord>> extensionLineConverters = new JDefaultDict<>(
				k -> getLineConverter(document, k));
		final JDefaultDict<DarwinCoreCoreOrExtension, Consumer<DarwinCoreRecord>> extensionResultConsumers = new JDefaultDict<>(
				k -> getResultConsumer(extensionQueues.get(k)));
		final JDefaultDict<DarwinCoreCoreOrExtension, Consumer<Reader>> extensionParseFunctions = new JDefaultDict<>(
				k -> DarwinCoreArchiveChecker.createParseFunction(k, h -> {
				}, extensionLineConverters.get(k), extensionResultConsumers.get(k), includeDefaults));

		return new CloseableIterator<List<DarwinCoreRecord>>() {

			private final AtomicBoolean started = new AtomicBoolean(false);
			private final CountDownLatch startCompleted = new CountDownLatch(1);
			private final AtomicBoolean closed = new AtomicBoolean(false);
			private volatile DarwinCoreRecord nextItem;
			private final ExecutorService executor = Executors.newFixedThreadPool(extensions.size() + 1);
			private final AtomicReference<Future<?>> coreParsingJob = new AtomicReference<>();
			private final JDefaultDict<DarwinCoreCoreOrExtension, AtomicReference<Future<?>>> extensionParsingJobs = new JDefaultDict<>(
					k -> new AtomicReference<>());

			private void doStart() {
				if (started.compareAndSet(false, true)) {
					try {
						Path nextMetadataPath = document.getMetadataXMLPath()
								.orElseThrow(() -> new IllegalStateException(
										"Metadata XML Path was null, not able to iterate due to a lack of a file reference point."));
						Future<?> previousJob = coreParsingJob.getAndSet(executor.submit(createParsingRunnable(document,
								document.getCore(), sentinel, pendingResults, parseFunction, nextMetadataPath)));
						cancelPreviousJob(previousJob, document.getCore());

						for (DarwinCoreCoreOrExtension nextExtension : extensions) {
							Future<?> previousExtensionJob = extensionParsingJobs.get(nextExtension)
									.getAndSet(executor.submit(createParsingRunnable(document, nextExtension, sentinel,
											extensionQueues.get(nextExtension),
											extensionParseFunctions.get(nextExtension), nextMetadataPath)));
							cancelPreviousJob(previousExtensionJob, nextExtension);
						}

						// No other jobs through this executor
						executor.shutdown();
					} finally {
						startCompleted.countDown();
					}
				}
			}

			private void cancelPreviousJob(Future<?> previousJob, DarwinCoreCoreOrExtension coreOrExtension) {
				// Should not have been more than one job due to setup
				// here, but avoiding the possibility that one could
				// leak in future
				if (previousJob != null) {
					System.out.println(
							"Found previous running parse for " + coreOrExtension + ", attempting to cancel it...");
					previousJob.cancel(true);
				}
			}

			private Runnable createParsingRunnable(final DarwinCoreArchiveDocument document,
					final DarwinCoreCoreOrExtension coreOrExtension, final DarwinCoreRecord sentinel,
					final BlockingQueue<DarwinCoreRecord> pendingResults, final Consumer<Reader> parseFunction,
					final Path nextMetadataPath) {
				return Unchecked.runnable(() -> {
					try {
						DarwinCoreArchiveChecker.parseCoreOrExtensionSorted(coreOrExtension, nextMetadataPath,
								parseFunction, false);
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						// Add a delay for adding the sentinel while the
						// queue is not yet empty
						int waitCount = 0;
						long waitTime = 10;
						while (pendingResults.size() > 0 && waitCount < 10) {
							Thread.sleep(waitTime);
							waitCount--;
						}
						pendingResults.offer(sentinel, 10, TimeUnit.SECONDS);
					}
				});
			}

			@Override
			public void close() {
				if (closed.compareAndSet(false, true)) {
					try {
						try {
							// Even if start wasn't called, need to be
							// consistent given there is no synchronisation or
							// locking involved other than
							// AtomicBoolean.compareAndSet, so start anyway here
							doStart();
							startCompleted.await();
						} finally {
							try {
								// If the sentinel could not be added in 10
								// seconds, attempt to clear the queue and try
								// again until space is available
								while (!pendingResults.offer(sentinel, 10, TimeUnit.SECONDS)) {
									pendingResults.clear();
								}
							} finally {
								try {
									executor.shutdown();
									executor.awaitTermination(10, TimeUnit.SECONDS);
									if (!executor.isTerminated()) {
										executor.shutdownNow();
									}
								} finally {
									Future<?> future = coreParsingJob.getAndSet(null);
									if (future != null && !future.isDone()) {
										future.cancel(true);
									}
								}
							}
						}
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						e.printStackTrace();
					}
				}
			}

			@Override
			public List<DarwinCoreRecord> next() {
				if (!closed.get()) {
					try {
						doStart();
						startCompleted.await();
						DarwinCoreRecord coreResult = nextItem;
						if (coreResult == null) {
							hasNext();
							coreResult = nextItem;
						}
						nextItem = null;
						if (coreResult == null) {
							throw new NoSuchElementException("Could not find next record");
						}
						return getResultList(coreResult);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
				throw new NoSuchElementException("No other records found");
			}

			/**
			 * Searches the extensions, which are sorted using the same criteria
			 * as the core to determine if they have a record with the required
			 * matching id and return them as part of the list, or omit them if
			 * they do not match.
			 * 
			 * @param coreResult The core record to match against extensions
			 * @return
			 */
			private List<DarwinCoreRecord> getResultList(DarwinCoreRecord coreResult) {
				return Arrays.asList(coreResult);
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
					doStart();
					startCompleted.await();
					if (nextItem != null) {
						return true;
					}
					DarwinCoreRecord poll = pendingResults.take();
					if (poll == sentinel || poll == null) {
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

	private Consumer<DarwinCoreRecord> getResultConsumer(final BlockingQueue<DarwinCoreRecord> pendingResults) {
		return l -> {
			try {
				pendingResults.put(l);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				e.printStackTrace();
			}
		};
	}

	private BiFunction<List<String>, List<String>, DarwinCoreRecord> getLineConverter(
			final DarwinCoreArchiveDocument document, final DarwinCoreCoreOrExtension coreOrExtension) {
		return (h, l) -> {
			// Enable interruption to fail the parse before it completes
			if (Thread.currentThread().isInterrupted()) {
				throw new IllegalStateException("Interruption occurred during parse");
			}
			return new DarwinCoreRecordImpl(document, coreOrExtension, l);
		};
	}

}
