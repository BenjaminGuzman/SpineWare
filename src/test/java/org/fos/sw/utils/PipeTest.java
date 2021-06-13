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

package org.fos.sw.utils;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class PipeTest
{
	private final Path inTmpPath = Paths.get(System.getProperty("java.io.tmpdir"), "pipe.test.in");
	private final Path outTmpPath = Paths.get(System.getProperty("java.io.tmpdir"), "pipe.test.out");
	private final Random random = new Random();

	PipeTest()
	{
	}

	public String randomString(int len)
	{
		return random.ints(10, 126 + 1)
		             .limit(len)
		             .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
		             .toString();
	}

	@BeforeEach
	void setUp() throws IOException
	{
		// clear contents
		FileWriter fiw = new FileWriter(inTmpPath.toFile());
		fiw.write("");
		fiw.close();

		// clear contents
		FileWriter fow = new FileWriter(outTmpPath.toFile());
		fow.write("");
		fow.close();
	}

	@Test()
	@DisplayName("Testing simple piping")
	void simple() throws InterruptedException, IOException
	{
		FileOutputStream fos = new FileOutputStream(outTmpPath.toFile());
		FileInputStream fis = new FileInputStream(inTmpPath.toFile());

		// write some random data
		final BufferedWriter writer =
			new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inTmpPath.toFile())));
		writer.write(randomString(Math.abs(random.nextInt(1_000))));
		writer.close();

		// start the pipe
		Thread t = new Pipe(new Pipe.Builder(fis, fos));
		t.start();
		t.join(); // wait till the pipe has been flushed

		// verify the pipe wrote everything
		assertLinesMatch(Files.readAllLines(inTmpPath), Files.readAllLines(outTmpPath));

		// both streams should be now closed
		boolean both_closed = true;
		try {
			fos.write(1);
			both_closed = false;
		} catch (IOException e) {
			assertEquals(e.getMessage(), "Stream Closed");
		}
		try {
			int i = fis.read();
			both_closed = false;
		} catch (IOException e) {
			assertEquals(e.getMessage(), "Stream Closed");
		}
		assertTrue(both_closed);
	}

	@Test()
	@DisplayName("Testing piping with prepend & append & close option")
	void test1() throws IOException, InterruptedException
	{
		FileOutputStream fos = new FileOutputStream(outTmpPath.toFile());
		FileInputStream fis = new FileInputStream(inTmpPath.toFile());

		// write some random data
		final BufferedWriter writer =
			new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inTmpPath.toFile())));
		writer.write(randomString(Math.abs(random.nextInt(1_000))));
		writer.close();

		// start the pipe
		String prependStr = "-Start-\n";
		String appendStr = "-End-\n";
		Thread t = new Pipe(
			new Pipe.Builder(fis, fos)
				.prependStr(prependStr)
				.appendStr(appendStr)
				.shouldCloseOutStream(false)
		);
		t.start();
		t.join(); // wait till the pipe has been flushed

		// verify the pipe wrote everything
		LinkedList<String> expectedLines = new LinkedList<>(Files.readAllLines(inTmpPath));
		expectedLines.addFirst(prependStr.replace("\n", ""));
		expectedLines.addLast(appendStr.replace("\n", ""));
		assertLinesMatch(expectedLines, Files.readAllLines(outTmpPath));

		// in stream should be closed by now
		try {
			int i = fis.read();
		} catch (IOException e) {
			assertEquals(e.getMessage(), "Stream Closed");
		}

		// but out stream should be open
		try {
			fos.write(1);
			assertTrue(true);
		} catch (IOException e) {
			fail();
		} finally {
			fos.close();
		}
	}

	@Test()
	@DisplayName("Testing piping with prefix & suffix")
	void test2() throws IOException, InterruptedException
	{
		// write some random data
		FileOutputStream fos = new FileOutputStream(outTmpPath.toFile());
		FileInputStream fis = new FileInputStream(inTmpPath.toFile());

		// write some random data
		final BufferedWriter writer =
			new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inTmpPath.toFile())));
		writer.write(randomString(Math.abs(random.nextInt(1_000))));
		writer.close();

		// start the pipe
		String prefix = "[Prefix]: ";
		String suffix = " :[Suffix]";
		Thread t = new Pipe(new Pipe.Builder(fis, fos).prefix(prefix).suffix(suffix));
		t.start();
		t.join(); // wait till the pipe has been flushed

		// verify the pipe wrote everything
		List<String> readLinesWithoutPrefix = Files.readAllLines(outTmpPath)
		                                           .stream()
		                                           .map((String line) -> {
			                                           assertTrue(line.startsWith(prefix));
			                                           assertTrue(line.endsWith(suffix));
			                                           return line.replace(prefix, "").replace(suffix, "");
		                                           })
		                                           .collect(Collectors.toList());
		assertLinesMatch(Files.readAllLines(inTmpPath), readLinesWithoutPrefix);
	}

	@Test()
	@DisplayName("Testing piping with human readable stuff")
	void testNoRandom() throws IOException, InterruptedException
	{
		// write some human readable data
		PrintWriter printWriter = new PrintWriter(inTmpPath.toFile());
		printWriter.println("Testing data");
		printWriter.println("Testing text");
		printWriter.close();

		FileOutputStream fos = new FileOutputStream(outTmpPath.toFile());
		FileInputStream fis = new FileInputStream(inTmpPath.toFile());

		// start the pipe
		String prefix = "[Prefix]: ";
		String suffix = " :[Suffix]";
		Thread t = new Pipe(
			new Pipe.Builder(fis, fos)
				.prefix(prefix)
				.suffix(suffix)
				.appendStr("-End-\n")
				.prependStr("-Start-\n")
		);
		t.start();
		t.join(); // wait till the pipe has been flushed

		// verify the pipe wrote everything
		assertEquals(
			"-Start-\n" +
				prefix + "Testing data" + suffix + System.lineSeparator() +
				prefix + "Testing text" + suffix + System.lineSeparator() +
				"-End-\n",
			new String(Files.readAllBytes(outTmpPath), StandardCharsets.UTF_8)
		);
	}
}