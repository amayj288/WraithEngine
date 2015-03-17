package wraith.library.AI;

import java.util.HashMap;

public class EvolutionaryNeuralNetwork{
	private double[][] weights;
	private final EvolutionaryLearningSystem learningSystem;
	private final BasicEvolutionProgressLog progressLog;
	private final int inputs, hiddenLayers, hiddenLayerSize, outputs;
	public EvolutionaryNeuralNetwork(int inputs, int hiddenLayers, int hiddenLayerSize, int outputs, double randomnessFactor){
		this.inputs=inputs;
		this.hiddenLayers=hiddenLayers;
		this.hiddenLayerSize=hiddenLayerSize;
		this.outputs=outputs;
		weights=new double[1+hiddenLayers][];
		int weightLinkCount = 0;
		for(int i = 0; i<=hiddenLayers; i++){
			if(i==0)weights[i]=new double[(inputs+1)*hiddenLayerSize];
			else if(i==hiddenLayers)weights[i]=new double[(hiddenLayerSize+1)*outputs];
			else weights[i]=new double[(hiddenLayerSize+1)*hiddenLayerSize];
			weightLinkCount+=weights[i].length;
		}
		learningSystem=new EvolutionaryLearningSystem(weightLinkCount, randomnessFactor, true);
		progressLog=new BasicEvolutionProgressLog(learningSystem);
		assignWeights();
	}
	private void assignWeights(){
		double[] values = learningSystem.next();
		int index = 0;
		for(int i = 0; i<=hiddenLayers; i++){
			for(int a = 0; a<weights[i].length; a++){
				weights[i][a]=values[index];
				index++;
			}
		}
	}
	public void score(long score){
		learningSystem.score(score);
		assignWeights();
	}
	public double[] run(double[] in){
		double[] lastData = new double[inputs+1];
		for(int a = 0; a<inputs; a++)lastData[a]=in[a];
		lastData[inputs]=1;
		for(int a = 0; a<=hiddenLayers; a++)lastData=compileLayer(lastData, a==hiddenLayers?outputs:hiddenLayerSize, a);
		return lastData;
	}
	private double[] compileLayer(double[] lastData, int layerSize, int layer){
		double[] out = new double[layerSize+(layer==hiddenLayers?0:1)];
		int weightIndex = 0;
		for(int a = 0; a<layerSize; a++){
			for(int b = 0; b<lastData.length; b++){
				out[a]+=lastData[b]*weights[layer][weightIndex];
				weightIndex++;
			}
			out[a]=activeFunction(out[a]);
		}
		if(layer!=hiddenLayers)out[out.length-1]=1;
		return out;
	}
	public void train(NeuralNetworkTrainingMatrix matrix, int passes){
		HashMap<double[],double[]> data = matrix.getData();
		for(int i = 0; i<passes; i++)score(calculateScore(data));
	}
	private long calculateScore(HashMap<double[],double[]> data){
		long l = 0;
		double[] b;
		for(double[] a : data.keySet()){
			b=data.get(a);
			l-=calculateError(a, b)*10000;
		}
		return l;
	}
	private double calculateError(double[] in, double[] out){
		double[] d = run(in);
		double l = 0;
		for(int a = 0; a<d.length; a++)l=Math.max(l, Math.abs(d[a]-out[a]));
		return l;
	}
	public int getInputs(){ return inputs; }
	public int getHiddenLayers(){ return hiddenLayers; }
	public int getHiddenLayerSize(){ return hiddenLayerSize; }
	public int getOutputs(){ return outputs; }
	public BasicEvolutionProgressLog getProgressLog(){ return progressLog; }
	private static double activeFunction(double d){ return 1/(1+Math.exp(-d)); }
}