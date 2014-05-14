package webfootprint.engine.lda;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.Formatter;
import java.util.Locale;

import java.util.concurrent.*;
import java.util.logging.*;
import java.util.regex.Pattern;
import java.util.zip.*;

import java.io.*;
import java.text.NumberFormat;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.types.*;
import cc.mallet.topics.TopicAssignment;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.util.Randoms;
import cc.mallet.util.MalletLogger;

public class ParallelTopicModel extends cc.mallet.topics.ParallelTopicModel {	
	
	public ParallelTopicModel (int numberOfTopics) {
		super(numberOfTopics);
	}

	private static final long serialVersionUID = 1;
	private static final int CURRENT_SERIAL_VERSION = 0;
	private static final int NULL_INTEGER = -1;

	private void writeObject (ObjectOutputStream out) throws IOException {
		out.writeInt (CURRENT_SERIAL_VERSION);
		
		out.writeObject(data.get(0).instance.getDataAlphabet());
		out.writeObject(alphabet);
		out.writeObject(topicAlphabet);

		out.writeInt(numTopics);

		out.writeInt(topicMask);
		out.writeInt(topicBits);

		out.writeInt(numTypes);

		out.writeObject(alpha);
		out.writeDouble(alphaSum);
		out.writeDouble(beta);
		out.writeDouble(betaSum);

		out.writeObject(typeTopicCounts);
		out.writeObject(tokensPerTopic);

		out.writeObject(docLengthCounts);
		out.writeObject(topicDocCounts);

		out.writeInt(numIterations);
		out.writeInt(burninPeriod);
		out.writeInt(saveSampleInterval);
		out.writeInt(optimizeInterval);
		out.writeInt(showTopicsInterval);
		out.writeInt(wordsPerTopic);

		out.writeInt(saveStateInterval);
		out.writeObject(stateFilename);

		out.writeInt(saveModelInterval);
		out.writeObject(modelFilename);

		out.writeInt(randomSeed);
		out.writeObject(formatter);
		//out.writeBoolean(printLogLikelihood);

		//out.writeInt(numThreads);
	}

	private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
		
		int version = in.readInt ();

		data = (ArrayList<TopicAssignment>) in.readObject ();
		alphabet = (Alphabet) in.readObject();
		topicAlphabet = (LabelAlphabet) in.readObject();
		
		numTopics = in.readInt();
		
		topicMask = in.readInt();
		topicBits = in.readInt();
		
		numTypes = in.readInt();
		
		alpha = (double[]) in.readObject();
		alphaSum = in.readDouble();
		beta = in.readDouble();
		betaSum = in.readDouble();
		
		typeTopicCounts = (int[][]) in.readObject();
		tokensPerTopic = (int[]) in.readObject();
		
		docLengthCounts = (int[]) in.readObject();
		topicDocCounts = (int[][]) in.readObject();
	
		numIterations = in.readInt();
		burninPeriod = in.readInt();
		saveSampleInterval = in.readInt();
		optimizeInterval = in.readInt();
		showTopicsInterval = in.readInt();
		wordsPerTopic = in.readInt();

		saveStateInterval = in.readInt();
		stateFilename = (String) in.readObject();
		
		saveModelInterval = in.readInt();
		modelFilename = (String) in.readObject();
		
		randomSeed = in.readInt();
		formatter = (NumberFormat) in.readObject();
		//printLogLikelihood = in.readBoolean();

		//numThreads = in.readInt();
	}
	
	public TopicInferencer readInferencer(ObjectInputStream in) throws IOException, ClassNotFoundException {
		
		double[]alpha = (double[]) in.readObject();
		Alphabet dataAlphabet = (Alphabet)in.readObject();
		double betaSum = in.readDouble();
		double beta = in.readDouble();
		int[][] typeTopicCounts = (int[][]) in.readObject();
		int[] tokensPerTopic = (int[]) in.readObject();
		return new TopicInferencer(typeTopicCounts, tokensPerTopic,
				   dataAlphabet,
				   alpha, beta, betaSum);
		
	}
	
	public void writeInferencer(ObjectOutputStream out) throws IOException {
		
		out.writeObject(alpha);
		out.writeObject(data.get(0).instance.getDataAlphabet());
		out.writeDouble(betaSum);
		out.writeDouble(beta);
		out.writeObject(typeTopicCounts);
		out.writeObject(tokensPerTopic);
	}
	
	public static void main (String[] args) {
		
		try {
			
			//InstanceList training = InstanceList.load (new File(args[0]));
			
			ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
			pipeList.add(new CharSequenceLowercase());
			pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
			pipeList.add(new TokenSequenceRemoveStopwords(new File("stoplists/en.txt"), "UTF-8", false, false, false));
			pipeList.add(new TokenSequence2FeatureSequence());

			InstanceList instances = new InstanceList (new SerialPipes(pipeList));
			File file = new File("text.txt");
			FileInputStream fileInputStream = new FileInputStream(file);
			InputStreamReader reader = new InputStreamReader(fileInputStream);
			//InputStream is = new ByteArrayInputStream(content.getBytes());
			instances.addThruPipe(new CsvIterator (reader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
												   3, 2, 1)); // data, label, name fields
			
			int numTopics = 10;
			
			ParallelTopicModel lda = new ParallelTopicModel (numTopics);
			lda.printLogLikelihood = true;
			lda.setTopicDisplay(50, 7);
			lda.addInstances(instances);
			
			lda.setNumThreads(2);
			lda.estimate();
			lda.writeInferencer(new ObjectOutputStream(new FileOutputStream(new File("inference.model"))));
			TopicInferencer restoredInferencer = lda.readInferencer(new ObjectInputStream(new FileInputStream(new File("inference.model"))));
			
			InstanceList secondInstances = new InstanceList (new SerialPipes(pipeList));
			BufferedReader in = new BufferedReader(new FileReader("text.txt"));
			String profile = in.readLine();
			in.close();
			
			reader = new InputStreamReader(new ByteArrayInputStream(profile.getBytes()));
			//InputStream is = new ByteArrayInputStream(content.getBytes());
			secondInstances.addThruPipe(new CsvIterator (reader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
												   3, 2, 1)); 
			Instance instance = instances.iterator().next();
			Instance secondInstance = secondInstances.iterator().next();
			double[] distribution = lda.getInferencer().getSampledDistribution(instance, 200, 100, 100);
			double[] secondDistribution = restoredInferencer.getSampledDistribution(secondInstance, 200, 100, 100);
			logger.info("printing state");
			lda.printState(new File("state.gz"));
			logger.info("finished printing");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
}
