/*
 * Copyright (c) 2021. Benjamín Antonio Velasco Guzmán
 * Author: Benjamín Antonio Velasco Guzmán <bg@benjaminguzman.dev>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.benjaminguzman.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.logging.Level;

import net.benjaminguzman.core.Loggers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Acts like a pipe. Redirects input from some stream to an output stream.
 * The redirection is done in another thread, therefore, when you call {@link Thread#start()}
 * this class starts reading input and redirecting.
 */
public class Pipe extends Thread
{
	private final Builder options;

	public Pipe(Builder options)
	{
		this.options = options;
		this.setDaemon(true);
		this.setName("Pipe-Thread");
	}

	@Override
	public void run()
	{
		final BufferedWriter writer = new BufferedWriter(
			new OutputStreamWriter(this.options.outStream, this.options.inStreamCharset)
		);
		try (BufferedReader reader = new BufferedReader(
			new InputStreamReader(this.options.inStream, this.options.outStreamCharset)
		)) {
			// write prepend string
			if (this.options.prependStr != null && !this.options.prependStr.isEmpty())
				writer.write(this.options.prependStr);

			// read from input stream and write to output stream
			String line;
			while ((line = reader.readLine()) != null) {
				if (this.options.prefix != null) writer.write(this.options.prefix);
				writer.write(line);
				if (this.options.suffix != null) writer.write(this.options.suffix);
				writer.newLine();
			}

			// write append string
			if (this.options.appendStr != null && !this.options.appendStr.isEmpty())
				writer.write(this.options.appendStr);
		} catch (IOException e) {
			if (this.options.onError != null)
				this.options.onError.accept(e);
		} finally {
			try {
				writer.flush();
				if (this.options.close_out_stream)
					writer.close();
			} catch (IOException e) {
				Loggers.getErrorLogger().log(
					Level.SEVERE,
					"Error while closing output stream in pipe",
					e
				);
			}
		}
	}

	public static class Builder
	{
		private final InputStream inStream;
		private final OutputStream outStream;
		private boolean close_out_stream = true;

		@Nullable
		private String prependStr;

		@Nullable
		private String appendStr;

		@Nullable
		private String prefix;

		@Nullable
		private String suffix;

		@Nullable
		private Consumer<Exception> onError;

		@NotNull
		private Charset inStreamCharset = StandardCharsets.UTF_8;

		@NotNull
		private Charset outStreamCharset = StandardCharsets.UTF_8;

		/**
		 * @param inStream  Data will be read from this stream
		 * @param outStream Read data will be written in this stream
		 */
		public Builder(@NotNull InputStream inStream, @NotNull OutputStream outStream)
		{
			this.inStream = inStream;
			this.outStream = outStream;
		}

		/**
		 * @param str string to be added to the start of the output
		 */
		public Builder prependStr(@NotNull String str)
		{
			this.prependStr = str;
			return this;
		}

		/**
		 * @param str string to be added to the end of the output
		 */
		public Builder appendStr(@NotNull String str)
		{
			this.appendStr = str;
			return this;
		}

		/**
		 * @param prefix prefix to be added to each line of the output
		 */
		public Builder prefix(@NotNull String prefix)
		{
			this.prefix = prefix;
			return this;
		}

		/**
		 * @param suffix suffix to be added to each line of the output
		 */
		public Builder suffix(@NotNull String suffix)
		{
			this.suffix = suffix;
			return this;
		}

		/**
		 * @param should_close Indicates whether or not the output stream should be closed when the input
		 *                     stream is also closed. Default: true
		 */
		public Builder shouldCloseOutStream(boolean should_close)
		{
			this.close_out_stream = should_close;
			return this;
		}

		/**
		 * @param inStreamCharset The default character set for the input stream. Default: UTF-8
		 */
		public Builder setInStreamCharset(Charset inStreamCharset)
		{
			this.inStreamCharset = inStreamCharset;
			return this;
		}

		/**
		 * @param outStreamCharset the default character set for the output stream. Default: UTF-8
		 */
		public Builder setOutStreamCharset(Charset outStreamCharset)
		{
			this.outStreamCharset = outStreamCharset;
			return this;
		}

		/**
		 * @param onError The callback to execute when an error occurred while reading from the input stream
		 *                or writing to the output stream
		 */
		public Builder onError(@NotNull Consumer<Exception> onError)
		{
			this.onError = onError;
			return this;
		}
	}
}
