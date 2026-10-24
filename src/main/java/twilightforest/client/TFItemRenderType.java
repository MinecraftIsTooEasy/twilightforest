package twilightforest.client;

/** Forge's ItemRenderType, reduced to the cases MITE can dispatch. */
public enum TFItemRenderType {
    ENTITY,
    EQUIPPED,
    EQUIPPED_FIRST_PERSON,
    INVENTORY,
    /** Map held in the first-person view; render data is {MapData, EntityPlayer, TextureManager}. */
    FIRST_PERSON_MAP
}
