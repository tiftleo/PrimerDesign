/*
 * Copyright (C) 2015  Santosh Gunturu <gunturus at msu dot edu>
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
package edu.msu.cme.rdp.primerdesign.screenoligos.oligo;

import edu.msu.cme.rdp.primerdesign.screenoligos.filter.OligoTempFilter;
import edu.msu.cme.rdp.primerdesign.screenoligos.filter.OligoBaseFilter;
import edu.msu.cme.rdp.primerdesign.utils.Primer3Wrapper;
import edu.msu.cme.rdp.readseq.utils.IUBUtilities;
import edu.msu.cme.rdp.readseq.readers.Sequence;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class establishes objects to build kmers, targetsets, and contains post
 * filtering
 *
 * @author gunturus, xingziye, tift
 */
public class EnumerateOligos {

    private Map<Integer, Set<Oligo>> kmerPosMap;
    private Map<Oligo, Set<String>> oligoTargetMap;
    private Map<Integer, Set<Oligo>> uniqueKmerPosMap;
    private List<Integer> kmerSizes;
    private List<Integer> positions;
    private List<OligoTempFilter> tempFilterList;
    private List<OligoBaseFilter> baseFilterList;
    private boolean reversed = false;
    private boolean gotRevPos = false;
    private int numSeq = 0;
    private Primer3Wrapper primer3Wrapper;
    private MismatchProperties calcObj;
    private int sequenceLength = 0;
    private int mismatches;

    public Map<Integer, Set<Oligo>> getKmerPosMap() {
        return kmerPosMap;
    }

    public Map<Oligo, Set<String>> getOligoTargetMap() {
        return oligoTargetMap;
    }

    public Map<Integer, Set<Oligo>> getUniqueKmerPosMap() {
        return uniqueKmerPosMap;
    }

    public List<Integer> getKmerSizes() {
        return kmerSizes;
    }

    public List<Integer> getPositions() {
        return positions;
    }

    public List<OligoTempFilter> getTempFilterList() {
        return tempFilterList;
    }

    public List<OligoBaseFilter> getBaseFilterList() {
        return baseFilterList;
    }

    public Primer3Wrapper getPrimer3Wrapper() {
        return primer3Wrapper;
    }

    public int getNumSeq() {
        return numSeq;
    }

    public int getSequenceLength() {
        return sequenceLength;
    }

    public int getMismatches() {
        return mismatches;
    }

    public void setKmerSizes(List<Integer> kmerSizes) {
        this.kmerSizes = kmerSizes;
    }

    public void setPositions(List<Integer> positions) {
        this.positions = positions;
    }

    public void setTempFilterList(List<OligoTempFilter> tempFilterList) {
        this.tempFilterList = tempFilterList;
    }

    public void setBaseFilterList(List<OligoBaseFilter> baseFilterList) {
        this.baseFilterList = baseFilterList;
    }

    public void setPrimer3Wrapper(Primer3Wrapper primer3Wrapper) {
        this.primer3Wrapper = primer3Wrapper;
    }

    public void setNumSeq(int numSeq) {
        this.numSeq = numSeq;
    }

    public void setReversed() {
        this.reversed = true;
    }

    /**
     *
     * @param primer3
     * @param kmerSizes
     * @param positions
     * @param filterList
     * @param reversed
     * @param mismatches
     * @param mismatchCalcProp
     * @throws IOException
     */
    public EnumerateOligos(Primer3Wrapper primer3, List<Integer> kmerSizes,
            List<Integer> positions, List<OligoTempFilter> tempFilterList, List<OligoBaseFilter> baseFilterList,
            boolean reversed, int mismatches, MismatchProperties mismatchCalcProp) throws IOException {

        this.calcObj = mismatchCalcProp;
        this.primer3Wrapper = primer3;
        this.kmerSizes = kmerSizes;
        this.positions = positions;
        this.tempFilterList = tempFilterList;
        this.baseFilterList = baseFilterList;
        this.reversed = reversed;
        this.kmerPosMap = new HashMap<>();
        this.oligoTargetMap = new HashMap<>();
        this.mismatches = mismatches;
    }

    public EnumerateOligos() throws IOException {
        calcObj = new MismatchProperties(new Oligo(""));
        primer3Wrapper = new Primer3Wrapper("mac");
        int[] sizes = {18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28};
        kmerSizes = new ArrayList();
        positions = new ArrayList();
        tempFilterList = new ArrayList();
        baseFilterList = new ArrayList();
        for (int size : sizes) {
            kmerSizes.add(size);
        }
        this.kmerPosMap = new HashMap<>();
        this.oligoTargetMap = new HashMap<>();
    }

    public void addKmers(List<Sequence> allSequences) throws IOException {

        numSeq = allSequences.size();
        sequenceLength = allSequences.get(0).getSeqString().length();

        if (positions.isEmpty()) {
            for (int i = 0; i < sequenceLength; i++) {
                positions.add(i);
            }
        }

        if (reversed) {
            List<Sequence> revSequences = new ArrayList<>();
            for (Sequence sequence : allSequences) {
                Sequence tempSequence = new Sequence(sequence.getSeqName(), sequence.getDesc(), IUBUtilities.reverseComplement(sequence.getSeqString()));
                revSequences.add(tempSequence);
            }
            allSequences = revSequences;

        }

        for (int currentpos : positions) {

            kmerPosMap.put(currentpos, new HashSet<Oligo>());

            for (Sequence sequence : allSequences) {
                String tempAlignSequence = sequence.getSeqString();

                StringBuilder stringBuilder = new StringBuilder();

                for (int j = currentpos; j < tempAlignSequence.length(); j++) {

                    if (tempAlignSequence.charAt(j) != '-' && tempAlignSequence.charAt(j) != '.') {
                        stringBuilder.append(tempAlignSequence.charAt(j));
                    }

                    if (stringBuilder.length() > kmerSizes.get(kmerSizes.size() - 1)) {
                        break;
                    } else if (stringBuilder.length() >= kmerSizes.get(0)) {
                        buildOligoTargetMap(stringBuilder, currentpos, sequence.getSeqName());
                    }

                }
            }

        }
    }

    public void buildOligoTargetMap(StringBuilder stringBuilder, int currentpos, String seqName) throws IOException {
        Oligo tempOligo;
        Set<String> tempTargetSet;
        Set<Oligo> tempOligoSet;

        String primerSeq = stringBuilder.toString().toUpperCase();

        tempOligo = OligoFactory.getOligoFromFactory(primerSeq, this.primer3Wrapper, this.calcObj, currentpos);

        if (!this.filterBaseOligos(tempOligo)) {
            return;
        }

        if (!this.filterTempOligos(tempOligo)) {
            return;
        }

        tempOligo.setStartPosition(currentpos);
        tempOligoSet = kmerPosMap.get(currentpos);
        tempOligoSet.add(tempOligo);
        kmerPosMap.put(currentpos, tempOligoSet);

        if (oligoTargetMap.containsKey(tempOligo)) {
            tempTargetSet = oligoTargetMap.get(tempOligo);
            tempTargetSet.add(seqName);
            oligoTargetMap.put(tempOligo, tempTargetSet);
        } else {
            tempTargetSet = new HashSet<>();
            tempTargetSet.add(seqName);
            oligoTargetMap.put(tempOligo, tempTargetSet);
        }

    }

    public boolean filterBaseOligos(Oligo oligo) {

        for (OligoBaseFilter filter : baseFilterList) {
            if (!filter.check(oligo)) {
                return false;
            }
        }
        return true;
    }

    public boolean filterTempOligos(Oligo oligo) {

        for (OligoTempFilter filter : tempFilterList) {
            if (!filter.check(oligo)) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @return a map with a set of targets as the key and a list of oligos
     * targeting that set of targets as the value; the list is sorted by the
     * melting temperature of oligo
     */
    public Map<Oligo, Set<String>> getTargetSetMap() {

        if (oligoTargetMap.isEmpty()) {
            return new HashMap<>();
        }

        Map<Oligo, Set<String>> oligoMap = new HashMap<>();

        for (Oligo oligo : oligoTargetMap.keySet()) {

            oligoMap.put(oligo, oligoTargetMap.get(oligo));

        }

        return oligoMap;

//        Map<Set<String>, ArrayList<Oligo>> targetSetMap = new HashMap<>();
//
//        for (Oligo oligo : oligoTargetMap.keySet()) {
//            Set<String> targetSet = oligoTargetMap.get(oligo);
//            if (targetSet.isEmpty()) {
//                continue;
//            }
//            if (targetSetMap.containsKey(targetSet)) {
//                targetSetMap.get(targetSet).add(oligo);
//            } else {
//                targetSetMap.put(targetSet, new ArrayList<Oligo>());
//                targetSetMap.get(targetSet).add(oligo);
//            }
//        }
//
//        return targetSetMap;
    }

    /**
     *
     * @param pos
     * @param targetSetMap
     * @return a map with a set of targets as the key and a list of oligos
     * targeting that set of targets as the value; the list is sorted by the
     * melting temperature of oligo Used in the sliding scale method
     */
    public Map<Oligo, Set<String>> getTargetSetMap(Integer pos) {

        if (kmerPosMap.get(pos) == null || kmerPosMap.get(pos).isEmpty()) {
            return new HashMap<>();
        }
        Map<Oligo, Set<String>> oligoMap = new HashMap<>();

        for (Oligo oligo : kmerPosMap.get(pos)) {

            oligoMap.put(oligo, oligoTargetMap.get(oligo));

        }

        return oligoMap;
    }

    /**
     * Used in OligosInfoGrapher during the screening process
     *
     * @param position
     * @param maxPrimerNum
     * @return double - coverage
     */
    public double calCoverage(int position, int maxPrimerNum) {

        Set<String> domain = new HashSet<>();
        Set<String> newDomain = new HashSet<>();

        for (Oligo oligo : kmerPosMap.get(position)) {
            domain.addAll(oligoTargetMap.get(oligo));
        }

        if (kmerPosMap.get(position).size() < maxPrimerNum) {
            return domain.size() / (double) numSeq;
        }

        Set<String> currentDomain = new HashSet<>(domain);
        int currentPrimNum = 0;

        while ((currentDomain.size()) != 0 && (currentPrimNum < maxPrimerNum)) {

            Set<String> maxDomain = new HashSet<>();

            for (Oligo oligo : kmerPosMap.get(position)) {

                Set<String> tempDomain = new HashSet<>(oligoTargetMap.get(oligo));
                tempDomain.retainAll(currentDomain);

                if (tempDomain.isEmpty()) {
                    continue;
                }

                if (tempDomain.size() > maxDomain.size()) {
                    maxDomain = tempDomain;
                }
            }

            newDomain.addAll(maxDomain);
            currentDomain.removeAll(maxDomain);
            currentPrimNum++;
        }

        return newDomain.size() / (double) numSeq;
    }

}
