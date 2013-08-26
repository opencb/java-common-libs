package org.opencb.commons.bioformats.commons.core.variant.io;

import java.io.IOException;
import java.util.List;

import org.opencb.commons.bioformats.commons.AbstractFormatReader;
import org.opencb.commons.bioformats.commons.core.variant.vcf4.VcfRecord;
import org.opencb.commons.bioformats.commons.exception.FileFormatException;

public class BcfReader extends AbstractFormatReader<VcfRecord> {

	/* (non-Javadoc)
	 * @see org.opencb.commons.bioformats.commons.AbstractFormatReader#size()
	 */
	@Override
	public int size() throws IOException, FileFormatException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.opencb.commons.bioformats.commons.AbstractFormatReader#read()
	 */
	@Override
	public VcfRecord read() throws FileFormatException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.opencb.commons.bioformats.commons.AbstractFormatReader#read(java.lang.String)
	 */
	@Override
	public VcfRecord read(String regexFilter) throws FileFormatException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.opencb.commons.bioformats.commons.AbstractFormatReader#read(int)
	 */
	@Override
	public List<VcfRecord> read(int size) throws FileFormatException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.opencb.commons.bioformats.commons.AbstractFormatReader#readAll()
	 */
	@Override
	public List<VcfRecord> readAll() throws FileFormatException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.opencb.commons.bioformats.commons.AbstractFormatReader#readAll(java.lang.String)
	 */
	@Override
	public List<VcfRecord> readAll(String pattern) throws FileFormatException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.opencb.commons.bioformats.commons.AbstractFormatReader#close()
	 */
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

}
