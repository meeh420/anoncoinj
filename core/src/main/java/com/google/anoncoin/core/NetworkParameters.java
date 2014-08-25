/**
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.anoncoin.core;

import com.google.common.base.Objects;
import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static com.google.anoncoin.core.Utils.COIN;
import static com.google.common.base.Preconditions.checkState;

/**
 * <p>NetworkParameters contains the data needed for working with an instantiation of a Anoncoin chain.</p>
 *
 * Currently there are only two, the production chain and the test chain. But in future as Anoncoin
 * evolves there may be more. You can create your own as long as they don't conflict.
 */
public class NetworkParameters implements Serializable {
    private static final long serialVersionUID = 3L;

    /**
     * The protocol version this library implements.
     */
    public static final int PROTOCOL_VERSION = 70007;

    /**
     * The alert signing key originally owned by Satoshi, and now passed on to Gavin along with a few others.
     */
    public static final byte[] SATOSHI_KEY = Hex.decode("04b2941a448ab9860beb73fa2f600c09bf9fe4d18d5ff0b3957bf94c6d177d61f88660d7c0dd9adef984080ddea03c898039759f66c2011c111c4394692f814962");

    /** The string returned by getId() for the main, production network where people trade things. */
    public static final String ID_PRODNET = "org.anoncoin.production";

    // TODO: Seed nodes should be here as well.

    // TODO: Replace with getters and then finish making all these fields final.

    /**
     * <p>Genesis block for this chain.</p>
     *
     * <p>The first block in every chain is a well known constant shared between all Anoncoin implemenetations. For a
     * block to be valid, it must be eventually possible to work backwards to the genesis block by following the
     * prevBlockHash pointers in the block headers.</p>
     *
     * <p>The genesis blocks for both test and prod networks contain the timestamp of when they were created,
     * and a message in the coinbase transaction. It says, <i>"The Times 03/Jan/2009 Chancellor on brink of second
     * bailout for banks"</i>.</p>
     */
    public final Block genesisBlock;
    /** What the easiest allowable proof of work should be. */
    public /*final*/ BigInteger proofOfWorkLimit;
    /** Default TCP port on which to connect to nodes. */
    public final int port;
    /** The header bytes that identify the start of a packet on this network. */
    public final long packetMagic;
    /**
     * First byte of a base58 encoded address. See {@link Address}. This is the same as acceptableAddressCodes[0] and
     * is the one used for "normal" addresses. Other types of address may be encountered with version codes found in
     * the acceptableAddressCodes array.
     */
    public final int addressHeader;
    /** First byte of a base58 encoded dumped private key. See {@link DumpedPrivateKey}. */
    public final int dumpedPrivateKeyHeader;
    /** How many blocks pass between difficulty adjustment periods. Anoncoin standardises this to be 2015. */
    public /*final*/ int interval;
    /**
     * How much time in seconds is supposed to pass between "interval" blocks. If the actual elapsed time is
     * significantly different from this value, the network difficulty formula will produce a different value. Both
     * test and production Anoncoin networks use 2 weeks (1209600 seconds).
     */
    public final int targetTimespan;
    /**
     * The key used to sign {@link AlertMessage}s. You can use {@link ECKey#verify(byte[], byte[], byte[])} to verify
     * signatures using it.
     */
    public /*final*/ byte[] alertSigningKey;

    /**
     * See getId(). This may be null for old deserialized wallets. In that case we derive it heuristically
     * by looking at the port number.
     */
    private final String id;

    /**
     * The depth of blocks required for a coinbase transaction to be spendable.
     */
    private final int spendableCoinbaseDepth;

    /**
     * Returns the number of blocks between subsidy decreases
     */
    private final int subsidyDecreaseBlockCount;

    /**
     * If we are running in testnet-in-a-box mode, we allow connections to nodes with 0 non-genesis blocks
     */
    final boolean allowEmptyPeerChains;

    /**
     * The version codes that prefix addresses which are acceptable on this network. Although Satoshi intended these to
     * be used for "versioning", in fact they are today used to discriminate what kind of data is contained in the
     * address and to prevent accidentally sending coins across chains which would destroy them.
     */
    public final int[] acceptableAddressCodes;


    /**
     * Block checkpoints are a safety mechanism that hard-codes the hashes of blocks at particular heights. Re-orgs
     * beyond this point will never be accepted. This field should be accessed using
     * {@link NetworkParameters#passesCheckpoint(int, Sha256Hash)} and {@link NetworkParameters#isCheckpoint(int)}.
     */
    public Map<Integer, Sha256Hash> checkpoints = new HashMap<Integer, Sha256Hash>();

    private NetworkParameters(int type) {
        alertSigningKey = SATOSHI_KEY;
        if (type == 0) {
            // Production.
            genesisBlock = createGenesis(this);
            interval = INTERVAL;
            targetTimespan = TARGET_TIMESPAN;
            proofOfWorkLimit = Utils.decodeCompactBits(0x1e0ffff0L);
            acceptableAddressCodes = new int[] { 23 };
            dumpedPrivateKeyHeader = 128;
            addressHeader = 23;
            port = 9377;
            packetMagic = 0xfacabada;
            genesisBlock.setDifficultyTarget(0x1e0ffff0L);
            genesisBlock.setTime(1370190760L);
            genesisBlock.setNonce(347089008L);
            genesisBlock.setMerkleRoot(new Sha256Hash("7ce7004d764515f9b43cb9f07547c8e2e00d94c9348b3da33c8681d350f2c736"));
            id = ID_PRODNET;
            subsidyDecreaseBlockCount = 306600;
            allowEmptyPeerChains = false;
            spendableCoinbaseDepth = 100;
            String genesisHash = genesisBlock.getHashAsString();
            checkState(genesisHash.equals("2c85519db50a40c033ccb3d4cb729414016afa537c66537f7d3d52dcd1d484a3"),
                    genesisHash);

            // This contains (at a minimum) the blocks which are not BIP30 compliant. BIP30 changed how duplicate
            // transactions are handled. Duplicated transactions could occur in the case where a coinbase had the same
            // extraNonce and the same outputs but appeared at different heights, and greatly complicated re-org handling.
            // Having these here simplifies block connection logic considerably.
            checkpoints.put(1, new Sha256Hash("8fe2901fc0999bc86ea2668c58802ee87165438166d18154f1bd4f917bf25e0f"));
            checkpoints.put(7, new Sha256Hash("4530df06d98fc77d04dab427630fc63b45f10d2b0ad3ad3a651883938986d629"));
            checkpoints.put(7777, new Sha256Hash("ae3094030b34a422c44b9832c84fe602d0d528449d6940374bd43b4472b4df5e"));
            checkpoints.put(15420, new Sha256Hash("fded6a374d071f59d738a3009fc4d8461609052c3e7e91aa89146550d179c1b0"));
            checkpoints.put(16000, new Sha256Hash("683517a8cae8530f39e636f010ecd1750665c3d91f57ba71d6556535972ab328"));
            checkpoints.put(77777, new Sha256Hash("f5c98062cb1ad75c792a1851a388447f0edd7cb2271b67ef1241a03c673b7735"));
            checkpoints.put(77778, new Sha256Hash("d13f93f9fdac82ea26ed8f90474ed2449c8c24be50a416e43c323a38573c30e5"));
            checkpoints.put(87142, new Sha256Hash("22c0aaf11b1e48ddbea702b4c44f78af90af9f53c25b9e3b347194dbd1b0ba0e")); // Last block before official build.
        } else {
            throw new RuntimeException();
        }
    }

    private static Block createGenesis(NetworkParameters n) {
        Block genesisBlock = new Block(n);
        Transaction t = new Transaction(n);
        try {
            // A script containing the difficulty bits and the following message:
            //
            //   "The Times 03/Jan/2009 Chancellor on brink of second bailout for banks"
            byte[] bytes = Hex.decode
                    ("04ffff001d01044c5e30322f4a756e2f323031333a202054686520556e6976657273652c20776527726520616c6c206f6e652e20427574207265616c6c792c206675636b207468652043656e7472616c2062616e6b732e202d20416e6f6e796d6f757320343230");
            t.addInput(new TransactionInput(n, t, bytes));
            ByteArrayOutputStream scriptPubKeyBytes = new ByteArrayOutputStream();
            Script.writeBytes(scriptPubKeyBytes, Hex.decode
                    ("00ac"));
            scriptPubKeyBytes.write(Script.OP_CHECKSIG);
            t.addOutput(new TransactionOutput(n, t, Utils.toNanoCoins(50, 0), scriptPubKeyBytes.toByteArray()));
        } catch (Exception e) {
            // Cannot happen.
            throw new RuntimeException(e);
        }
        genesisBlock.addTransaction(t);
        return genesisBlock;
    }

    public static final int TARGET_TIMESPAN = (int)86184;  // 420 per difficulty cycle, on average.
    public static final int TARGET_SPACING = (int)205;  // 3.42 minutes per block.
    public static final int INTERVAL = TARGET_TIMESPAN / TARGET_SPACING;

    /**
     * Blocks with a timestamp after this should enforce BIP 16, aka "Pay to script hash". This BIP changed the
     * network rules in a soft-forking manner, that is, blocks that don't follow the rules are accepted but not
     * mined upon and thus will be quickly re-orged out as long as the majority are enforcing the rule.
     */
    public static final int BIP16_ENFORCE_TIME = 1333238400;

    /**
     * The maximum money to be generated
     */
    public static final BigInteger MAX_MONEY = new BigInteger("4200000", 10).multiply(COIN);

    private static NetworkParameters pn;
    /** The primary Anoncoin chain created by Satoshi. */
    public synchronized static NetworkParameters prodNet() {
        if (pn == null) {
            pn = new NetworkParameters(0);
        }
        return pn;
    }

    /**
     * A Java package style string acting as unique ID for these parameters
     */
    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof NetworkParameters)) return false;
        NetworkParameters o = (NetworkParameters) other;
        return o.getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    /** Returns the network parameters for the given string ID or NULL if not recognized. */
    public static NetworkParameters fromID(String id) {
        if (id.equals(ID_PRODNET)) {
            return prodNet();
        } else {
            return null;
        }
    }

    public int getSpendableCoinbaseDepth() {
        return spendableCoinbaseDepth;
    }

    /**
     * Returns true if the block height is either not a checkpoint, or is a checkpoint and the hash matches.
     */
    public boolean passesCheckpoint(int height, Sha256Hash hash) {
        Sha256Hash checkpointHash = checkpoints.get(height);
        return checkpointHash == null || checkpointHash.equals(hash);
    }

    /**
     * Returns true if the given height has a recorded checkpoint.
     */
    public boolean isCheckpoint(int height) {
        Sha256Hash checkpointHash = checkpoints.get(height);
        return checkpointHash != null;
    }

    public int getSubsidyDecreaseBlockCount() {
        return subsidyDecreaseBlockCount;
    }
}
