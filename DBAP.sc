DBAPSpeakerArray //a convinient class just to set the speakers position in x, y
{
	var <>arrayOfPositionsXY;
	*new
	{
		arg arrayOfPositionsXY = #[];
		^super.new.initDBAPSpeakerArray(arrayOfPositionsXY);
	}
	initDBAPSpeakerArray
	{
		|positions|
		arrayOfPositionsXY = positions;
		^arrayOfPositionsXY;
	}
}

DBAPsrc //actual class for the single source
{
	var <>id, <>bus, <>dbapArr;
	var <>vi, <>dist, <>r;
	var <>synth;
	var <>a;
	var x, y;

	*new
	{
		|id, x, y, dbapArr, r|
		^super.new.initDBAPsrc(id, x, y, dbapArr, r);
	}


	initDBAPsrc
	{
		|anId, aX, aY, aDbapspkrArr, aR|
		r = aR; //spatial blur coefficient
		id = anId; //id
		x = aX;
		y = aY;
		dbapArr = aDbapspkrArr; //x, y positions of the speakers as instance of DBAPSpeakerArray
		vi = Array.newClear(dbapArr.size);
		synth = Array.newClear(dbapArr.size);
		dist = Array.newClear(dbapArr.size);
		Server.local.waitForBoot
		{
			SynthDef(\dbapSpkr, { //speaker SynthDef
				arg in, amp, out;
				var sig;
				sig = In.ar(in)*amp; //scaled output of the calculed coefficient
				Out.ar(out, sig);
			}).add;
			a = this.calcV(x, y); //coefficient for each speaker
			bus = Bus.audio(Server.local, 1);
			Server.local.sync;
			dbapArr.do({
				|item, i|
				synth[i] = Synth(\dbapSpkr, [\in, bus, \amp, a[i], \out, i], addAction:\addToTail); //synth for each speaker with right parameters
			});
		}
	}

	spkrArr{
		dbapArr.do({
			|item, i|
			"Speaker number:".scatArgs(i+1).postln;
			item.do({
				|jtem, j|
				case
				{j == 0}{"X:".scatArgs(jtem).postln}
				{j == 1}{"Y:".scatArgs(jtem).postln};
			});
			" ".postln;
		});
	}

	calcDist
	{
		|xS, yS, x, y, r|
		var d = ((((x-xS)**2)+((y-yS)**2))+(r**2)).sqrt;
		^d;
	}

	calcV
	{
		|x, y|
		var denK = 0, k;
		var a = 10**(-6.dbamp/20); //<--amplitude roll-off
		dbapArr.do({
			|item, i|
			dist[i] = this.calcDist(x, y, item[0], item[1], r);
			denK = denK + (1/(dist[i]**2));
		});
		k = (2*a)/(denK.sqrt); //<-- k, positional coefficient
		dbapArr.do({
			|item, i|
			var d;
			d = this.calcDist(x, y, item[0], item[1], r);
			vi[i] = k/(2*d*a); //<-- relative amplitude for the ith speaker
		});
		^vi; //<-- all of vi**2 sum together should be 1 (or 0.99999)
	}

	xy_{ //change the x, y coordinates of the src
		|newX, newY|
		var change = [];
		change = this.calcV(newX, newY); //calculate again x and y
		dbapArr.do({
			|item, i|
			synth[i].set(\amp, change[i]);
		});
		^change;
	}

	nSpeakers{^dbapArr.size}
}
/*
SynthDef(\test, { //actual synth
	arg out, freq = 100;
	var sig;
	sig = SinOsc.ar(freq)*LFPulse.kr(1);
	Out.ar(out, sig);
}).add;

x = DBAPSpeakerArray.new([[-1, 1], [1, 1], [1, -1], [-1, -1]]); //<-- set speakers position (this case quadriphony)

c = DBAPsrc.new(1, 1, 1, x, 0.01); // <-- id, initial x and y, DBAPSpeakerArray instance, spatial blur

Synth(\test, [\out, c.bus, \freq, 1000]); //<-- routes the synths out to the class' bus

c.xy_(-1, -0) //<-- new position of the source

~trajectory = { //<-- trajectory function
	arg x1, y1, x2, y2, //starting and ending point
	dur, //duration
	curve = \exp, //type of curve
	src, //who to spatialize
	sr = 0.01; //rate for movement
	var xLenght = x2-x1 ; //total lenght of the trajectory
	var numPoints = (dur/sr).asInteger ; //number of points of the envelope
	var xStep = xLenght / numPoints ;
	var env = Env([y1, y2], [1], curve).asSignal(numPoints).asArray; //the envelope
	{
		src.xy_(x1, y1);
		env.do{ //the update routine
			|i, j|
			src.xy_(xStep*j+x1, i);
			//updated values
			sr.wait; //waits sr time
		};
	}.fork(AppClock)
};

~trajectory.(-1, 1, -1, -1, 5, \exp, c) <--some trajectories
~trajectory.(-1, -1, -1, 1, 5, \exp, c)
~trajectory.(-1, 1, 1, 1, 5, \exp, c)
~trajectory.(1, 1, -1, 1, 5, \exp, c)
~trajectory.(-1, 1, 1, -1, 5, \exp, c)
~trajectory.(1, -1, -1, 1, 5, \exp, c)


*/

/*
DBAPSpeaker
{
var <>x, <>y;
*new
{
|x, y|
^super.new.initDBAPSpeaker(x, y);
}


initDBAPSpeaker
{
|xS, yS|
x = xS;
y = yS;
}


}
*/