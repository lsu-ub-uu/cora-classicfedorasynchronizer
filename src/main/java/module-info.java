module se.uu.ub.cora.classicfedorasynchronizer {
	requires se.uu.ub.cora.xmlutils;
	requires transitive se.uu.ub.cora.data;
	requires se.uu.ub.cora.httphandler;
	requires se.uu.ub.cora.storage;
	requires se.uu.ub.cora.converter;
	requires se.uu.ub.cora.messaging;
	requires se.uu.ub.cora.javaclient;
	requires se.uu.ub.cora.sqlstorage;
	requires se.uu.ub.cora.sqldatabase;
	requires se.uu.ub.cora.fedoralegacy;

	exports se.uu.ub.cora.classicfedorasynchronizer.messaging;
}