# dbapSC
Easy implementation of DBAP spatialization

DBAPSpeakerArray is the class to set the position of the n speakers

DBAPsrc is the class that does all the calculation and receives the input audio stream. It uses an id, x and y positions, an instance of DBAPSPeakerArray and a spatial blur coefficient.

DBAPPlot is the visualization of the data being spatialized, both speakers and sources.

In the current state it cannot manage sources that work outside the specified field of speakers!

The implementation is based on the paper http://www.pnek.org/wp-content/uploads/2010/04/icmc2009-dbap.pdf
