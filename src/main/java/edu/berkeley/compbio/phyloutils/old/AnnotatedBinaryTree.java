package edu.berkeley.compbio.phyloutils.old;

import com.davidsoergel.dsutils.DSArrayUtils;
import org.apache.log4j.Logger;

/**
 * A Tree on which some number of independent characters are evolving in parallel.
 *
 * @author lorax
 * @version 1.0
 */
@Deprecated
public class AnnotatedBinaryTree extends BinaryTree
	{
	private static final Logger logger = Logger.getLogger(AnnotatedBinaryTree.class);

	private CharacterDistributionOnTree[] characters;

	public AnnotatedBinaryTree(int nodes, int chars)
		{
		super(nodes);
		characters = new CharacterDistributionOnTree[chars];
		}

	public void setCharacter(int num, CharacterDistributionOnTree cdt)

		{
		characters[num] = cdt;
		}

	public CharacterDistributionOnTree[] getCharacters()
		{
		if (DSArrayUtils.contains(characters, null))
			{

			logger.warn("Not all characters have been set");
			}
		return characters;
		}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String nodeInfo(int node)
		{
		return "" + (characters[0].get(node) == 1 ? "*" : "-");
		}
	}
