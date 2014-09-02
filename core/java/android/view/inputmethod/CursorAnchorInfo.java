/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package android.view.inputmethod;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Layout;
import android.text.SpannedString;
import android.text.TextUtils;
import android.view.inputmethod.SparseRectFArray.SparseRectFArrayBuilder;

import java.util.Objects;

/**
 * Positional information about the text insertion point and characters in the composition string.
 *
 * <p>This class encapsulates locations of the text insertion point and the composition string in
 * the screen coordinates so that IMEs can render their UI components near where the text is
 * actually inserted.</p>
 */
public final class CursorAnchorInfo implements Parcelable {
    /**
     * The index of the first character of the selected text (inclusive). {@code -1} when there is
     * no text selection.
     */
    private final int mSelectionStart;
    /**
     * The index of the first character of the selected text (exclusive). {@code -1} when there is
     * no text selection.
     */
    private final int mSelectionEnd;

    /**
     * The index of the first character of the composing text (inclusive). {@code -1} when there is
     * no composing text.
     */
    private final int mComposingTextStart;
    /**
     * The text, tracked as a composing region.
     */
    private final CharSequence mComposingText;

    /**
     * Flags of the insertion marker. See {@link #FLAG_HAS_VISIBLE_REGION} for example.
     */
    private final int mInsertionMarkerFlags;
    /**
     * Horizontal position of the insertion marker, in the local coordinates that will be
     * transformed with the transformation matrix when rendered on the screen. This should be
     * calculated or compatible with {@link Layout#getPrimaryHorizontal(int)}. This can be
     * {@code java.lang.Float.NaN} when no value is specified.
     */
    private final float mInsertionMarkerHorizontal;
    /**
     * Vertical position of the insertion marker, in the local coordinates that will be
     * transformed with the transformation matrix when rendered on the screen. This should be
     * calculated or compatible with {@link Layout#getLineTop(int)}. This can be
     * {@code java.lang.Float.NaN} when no value is specified.
     */
    private final float mInsertionMarkerTop;
    /**
     * Vertical position of the insertion marker, in the local coordinates that will be
     * transformed with the transformation matrix when rendered on the screen. This should be
     * calculated or compatible with {@link Layout#getLineBaseline(int)}. This can be
     * {@code java.lang.Float.NaN} when no value is specified.
     */
    private final float mInsertionMarkerBaseline;
    /**
     * Vertical position of the insertion marker, in the local coordinates that will be
     * transformed with the transformation matrix when rendered on the screen. This should be
     * calculated or compatible with {@link Layout#getLineBottom(int)}. This can be
     * {@code java.lang.Float.NaN} when no value is specified.
     */
    private final float mInsertionMarkerBottom;

    /**
     * Container of rectangular position of characters, keyed with character index in a unit of
     * Java chars, in the local coordinates that will be transformed with the transformation matrix
     * when rendered on the screen.
     */
    private final SparseRectFArray mCharacterBoundsArray;

    /**
     * Transformation matrix that is applied to any positional information of this class to
     * transform local coordinates into screen coordinates.
     */
    private final Matrix mMatrix;

    /**
     * Flag for {@link #getInsertionMarkerFlags()} and {@link #getCharacterBoundsFlags(int)}: the
     * insertion marker or character bounds have at least one visible region.
     */
    public static final int FLAG_HAS_VISIBLE_REGION = 0x01;

    /**
     * Flag for {@link #getInsertionMarkerFlags()} and {@link #getCharacterBoundsFlags(int)}: the
     * insertion marker or character bounds have at least one invisible (clipped) region.
     */
    public static final int FLAG_HAS_INVISIBLE_REGION = 0x02;

    /**
     * Flag for {@link #getInsertionMarkerFlags()} and {@link #getCharacterBoundsFlags(int)}: the
     * insertion marker or character bounds is placed at right-to-left (RTL) character.
     */
    public static final int FLAG_IS_RTL = 0x04;

    /**
     * @removed
     */
    public static final int CHARACTER_RECT_TYPE_MASK = 0x0f;
    /**
     * Type for {@link #CHARACTER_RECT_TYPE_MASK}: the editor did not specify any type of this
     * character. Editor authors should not use this flag.
     * @removed
     */
    public static final int CHARACTER_RECT_TYPE_UNSPECIFIED = 0;
    /**
     * Type for {@link #CHARACTER_RECT_TYPE_MASK}: the character is entirely visible.
     * @removed
     */
    public static final int CHARACTER_RECT_TYPE_FULLY_VISIBLE = 1;
    /**
     * Type for {@link #CHARACTER_RECT_TYPE_MASK}: some area of the character is invisible.
     * @removed
     */
    public static final int CHARACTER_RECT_TYPE_PARTIALLY_VISIBLE = 2;
    /**
     * Type for {@link #CHARACTER_RECT_TYPE_MASK}: the character is entirely invisible.
     * @removed
     */
    public static final int CHARACTER_RECT_TYPE_INVISIBLE = 3;
    /**
     * Type for {@link #CHARACTER_RECT_TYPE_MASK}: the editor gave up to calculate the rectangle
     * for this character. Input method authors should ignore the returned rectangle.
     * @removed
     */
    public static final int CHARACTER_RECT_TYPE_NOT_FEASIBLE = 4;

    public CursorAnchorInfo(final Parcel source) {
        mSelectionStart = source.readInt();
        mSelectionEnd = source.readInt();
        mComposingTextStart = source.readInt();
        mComposingText = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
        mInsertionMarkerFlags = source.readInt();
        mInsertionMarkerHorizontal = source.readFloat();
        mInsertionMarkerTop = source.readFloat();
        mInsertionMarkerBaseline = source.readFloat();
        mInsertionMarkerBottom = source.readFloat();
        mCharacterBoundsArray = source.readParcelable(SparseRectFArray.class.getClassLoader());
        mMatrix = new Matrix();
        mMatrix.setValues(source.createFloatArray());
    }

    /**
     * Used to package this object into a {@link Parcel}.
     *
     * @param dest The {@link Parcel} to be written.
     * @param flags The flags used for parceling.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mSelectionStart);
        dest.writeInt(mSelectionEnd);
        dest.writeInt(mComposingTextStart);
        TextUtils.writeToParcel(mComposingText, dest, flags);
        dest.writeInt(mInsertionMarkerFlags);
        dest.writeFloat(mInsertionMarkerHorizontal);
        dest.writeFloat(mInsertionMarkerTop);
        dest.writeFloat(mInsertionMarkerBaseline);
        dest.writeFloat(mInsertionMarkerBottom);
        dest.writeParcelable(mCharacterBoundsArray, flags);
        final float[] matrixArray = new float[9];
        mMatrix.getValues(matrixArray);
        dest.writeFloatArray(matrixArray);
    }

    @Override
    public int hashCode(){
        final float floatHash = mInsertionMarkerHorizontal + mInsertionMarkerTop
                + mInsertionMarkerBaseline + mInsertionMarkerBottom;
        int hash = floatHash > 0 ? (int) floatHash : (int)(-floatHash);
        hash *= 31;
        hash += mInsertionMarkerFlags;
        hash *= 31;
        hash += mSelectionStart + mSelectionEnd + mComposingTextStart;
        hash *= 31;
        hash += Objects.hashCode(mComposingText);
        hash *= 31;
        hash += Objects.hashCode(mCharacterBoundsArray);
        hash *= 31;
        hash += Objects.hashCode(mMatrix);
        return hash;
    }

    /**
     * Compares two float values. Returns {@code true} if {@code a} and {@code b} are
     * {@link Float#NaN} at the same time.
     */
    private static boolean areSameFloatImpl(final float a, final float b) {
        if (Float.isNaN(a) && Float.isNaN(b)) {
            return true;
        }
        return a == b;
    }

    @Override
    public boolean equals(Object obj){
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CursorAnchorInfo)) {
            return false;
        }
        final CursorAnchorInfo that = (CursorAnchorInfo) obj;
        if (hashCode() != that.hashCode()) {
            return false;
        }
        if (mSelectionStart != that.mSelectionStart || mSelectionEnd != that.mSelectionEnd) {
            return false;
        }
        if (mComposingTextStart != that.mComposingTextStart
                || !Objects.equals(mComposingText, that.mComposingText)) {
            return false;
        }
        if (mInsertionMarkerFlags != that.mInsertionMarkerFlags
                || !areSameFloatImpl(mInsertionMarkerHorizontal, that.mInsertionMarkerHorizontal)
                || !areSameFloatImpl(mInsertionMarkerTop, that.mInsertionMarkerTop)
                || !areSameFloatImpl(mInsertionMarkerBaseline, that.mInsertionMarkerBaseline)
                || !areSameFloatImpl(mInsertionMarkerBottom, that.mInsertionMarkerBottom)) {
            return false;
        }
        if (!Objects.equals(mCharacterBoundsArray, that.mCharacterBoundsArray)) {
            return false;
        }
        if (!Objects.equals(mMatrix, that.mMatrix)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SelectionInfo{mSelection=" + mSelectionStart + "," + mSelectionEnd
                + " mComposingTextStart=" + mComposingTextStart
                + " mComposingText=" + Objects.toString(mComposingText)
                + " mInsertionMarkerFlags=" + mInsertionMarkerFlags
                + " mInsertionMarkerHorizontal=" + mInsertionMarkerHorizontal
                + " mInsertionMarkerTop=" + mInsertionMarkerTop
                + " mInsertionMarkerBaseline=" + mInsertionMarkerBaseline
                + " mInsertionMarkerBottom=" + mInsertionMarkerBottom
                + " mCharacterBoundsArray=" + Objects.toString(mCharacterBoundsArray)
                + " mMatrix=" + Objects.toString(mMatrix)
                + "}";
    }

    /**
     * Builder for {@link CursorAnchorInfo}. This class is not designed to be thread-safe.
     */
    public static final class Builder {
        private int mSelectionStart = -1;
        private int mSelectionEnd = -1;
        private int mComposingTextStart = -1;
        private CharSequence mComposingText = null;
        private float mInsertionMarkerHorizontal = Float.NaN;
        private float mInsertionMarkerTop = Float.NaN;
        private float mInsertionMarkerBaseline = Float.NaN;
        private float mInsertionMarkerBottom = Float.NaN;
        private int mInsertionMarkerFlags = 0;
        private SparseRectFArrayBuilder mCharacterBoundsArrayBuilder = null;
        private final Matrix mMatrix = new Matrix(Matrix.IDENTITY_MATRIX);
        private boolean mMatrixInitialized = false;

        /**
         * Sets the text range of the selection. Calling this can be skipped if there is no
         * selection.
         */
        public Builder setSelectionRange(final int newStart, final int newEnd) {
            mSelectionStart = newStart;
            mSelectionEnd = newEnd;
            return this;
        }

        /**
         * Sets the text range of the composing text. Calling this can be skipped if there is
         * no composing text.
         * @param composingTextStart index where the composing text starts.
         * @param composingText the entire composing text.
         */
        public Builder setComposingText(final int composingTextStart,
            final CharSequence composingText) {
            mComposingTextStart = composingTextStart;
            if (composingText == null) {
                mComposingText = null;
            } else {
                // Make a snapshot of the given char sequence.
                mComposingText = new SpannedString(composingText);
            }
            return this;
        }

        /**
         * @removed
         */
        public Builder setInsertionMarkerLocation(final float horizontalPosition,
                final float lineTop, final float lineBaseline, final float lineBottom,
                final boolean clipped){
            mInsertionMarkerHorizontal = horizontalPosition;
            mInsertionMarkerTop = lineTop;
            mInsertionMarkerBaseline = lineBaseline;
            mInsertionMarkerBottom = lineBottom;
            mInsertionMarkerFlags = clipped ? FLAG_HAS_INVISIBLE_REGION : 0;
            return this;
        }

        /**
         * Sets the location of the text insertion point (zero width cursor) as a rectangle in
         * local coordinates. Calling this can be skipped when there is no text insertion point;
         * however if there is an insertion point, editors must call this method.
         * @param horizontalPosition horizontal position of the insertion marker, in the local
         * coordinates that will be transformed with the transformation matrix when rendered on the
         * screen. This should be calculated or compatible with
         * {@link Layout#getPrimaryHorizontal(int)}.
         * @param lineTop vertical position of the insertion marker, in the local coordinates that
         * will be transformed with the transformation matrix when rendered on the screen. This
         * should be calculated or compatible with {@link Layout#getLineTop(int)}.
         * @param lineBaseline vertical position of the insertion marker, in the local coordinates
         * that will be transformed with the transformation matrix when rendered on the screen. This
         * should be calculated or compatible with {@link Layout#getLineBaseline(int)}.
         * @param lineBottom vertical position of the insertion marker, in the local coordinates
         * that will be transformed with the transformation matrix when rendered on the screen. This
         * should be calculated or compatible with {@link Layout#getLineBottom(int)}.
         * @param flags flags of the insertion marker. See {@link #FLAG_HAS_VISIBLE_REGION} for
         * example.
         */
        public Builder setInsertionMarkerLocation(final float horizontalPosition,
                final float lineTop, final float lineBaseline, final float lineBottom,
                final int flags){
            mInsertionMarkerHorizontal = horizontalPosition;
            mInsertionMarkerTop = lineTop;
            mInsertionMarkerBaseline = lineBaseline;
            mInsertionMarkerBottom = lineBottom;
            mInsertionMarkerFlags = flags;
            return this;
        }

        /**
         * Adds the bounding box of the character specified with the index.
         *
         * @param index index of the character in Java chars units. Must be specified in
         * ascending order across successive calls.
         * @param left x coordinate of the left edge of the character in local coordinates.
         * @param top y coordinate of the top edge of the character in local coordinates.
         * @param right x coordinate of the right edge of the character in local coordinates.
         * @param bottom y coordinate of the bottom edge of the character in local coordinates.
         * @param flags flags for this character bounds. See {@link #FLAG_HAS_VISIBLE_REGION},
         * {@link #FLAG_HAS_INVISIBLE_REGION} and {@link #FLAG_IS_RTL}. These flags must be
         * specified when necessary.
         * @throws IllegalArgumentException If the index is a negative value, or not greater than
         * all of the previously called indices.
         */
        public Builder addCharacterBounds(final int index, final float left, final float top,
                final float right, final float bottom, final int flags) {
            if (index < 0) {
                throw new IllegalArgumentException("index must not be a negative integer.");
            }
            if (mCharacterBoundsArrayBuilder == null) {
                mCharacterBoundsArrayBuilder = new SparseRectFArrayBuilder();
            }
            mCharacterBoundsArrayBuilder.append(index, left, top, right, bottom, flags);
            return this;
        }

        /**
         * Adds the bounding box of the character specified with the index.
         *
         * @param index index of the character in Java chars units. Must be specified in
         * ascending order across successive calls.
         * @param leadingEdgeX x coordinate of the leading edge of the character in local
         * coordinates, that is, left edge for LTR text and right edge for RTL text.
         * @param leadingEdgeY y coordinate of the leading edge of the character in local
         * coordinates.
         * @param trailingEdgeX x coordinate of the trailing edge of the character in local
         * coordinates, that is, right edge for LTR text and left edge for RTL text.
         * @param trailingEdgeY y coordinate of the trailing edge of the character in local
         * coordinates.
         * @param flags flags for this character rect. See {@link #FLAG_HAS_VISIBLE_REGION} for
         * example.
         * @throws IllegalArgumentException If the index is a negative value, or not greater than
         * all of the previously called indices.
         * @removed
         */
        public Builder addCharacterRect(final int index, final float leadingEdgeX,
                final float leadingEdgeY, final float trailingEdgeX, final float trailingEdgeY,
                final int flags) {
            final int newFlags;
            final float left;
            final float right;
            if (leadingEdgeX <= trailingEdgeX) {
                newFlags = flags;
                left = leadingEdgeX;
                right = trailingEdgeX;
            } else {
                newFlags = flags | FLAG_IS_RTL;
                left = trailingEdgeX;
                right = leadingEdgeX;
            }
            return addCharacterBounds(index, left, leadingEdgeY, right, trailingEdgeY, newFlags);
        }

        /**
         * Sets the matrix that transforms local coordinates into screen coordinates.
         * @param matrix transformation matrix from local coordinates into screen coordinates. null
         * is interpreted as an identity matrix.
         */
        public Builder setMatrix(final Matrix matrix) {
            mMatrix.set(matrix != null ? matrix : Matrix.IDENTITY_MATRIX);
            mMatrixInitialized = true;
            return this;
        }

        /**
         * @return {@link CursorAnchorInfo} using parameters in this {@link Builder}.
         * @throws IllegalArgumentException if one or more positional parameters are specified but
         * the coordinate transformation matrix is not provided via {@link #setMatrix(Matrix)}.
         */
        public CursorAnchorInfo build() {
            if (!mMatrixInitialized) {
                // Coordinate transformation matrix is mandatory when at least one positional
                // parameter is specified.
                final boolean hasCharacterBounds = (mCharacterBoundsArrayBuilder != null
                        && !mCharacterBoundsArrayBuilder.isEmpty());
                if (hasCharacterBounds
                        || !Float.isNaN(mInsertionMarkerHorizontal)
                        || !Float.isNaN(mInsertionMarkerTop)
                        || !Float.isNaN(mInsertionMarkerBaseline)
                        || !Float.isNaN(mInsertionMarkerBottom)) {
                    throw new IllegalArgumentException("Coordinate transformation matrix is " +
                            "required when positional parameters are specified.");
                }
            }
            return new CursorAnchorInfo(this);
        }

        /**
         * Resets the internal state so that this instance can be reused to build another
         * instance of {@link CursorAnchorInfo}.
         */
        public void reset() {
            mSelectionStart = -1;
            mSelectionEnd = -1;
            mComposingTextStart = -1;
            mComposingText = null;
            mInsertionMarkerFlags = 0;
            mInsertionMarkerHorizontal = Float.NaN;
            mInsertionMarkerTop = Float.NaN;
            mInsertionMarkerBaseline = Float.NaN;
            mInsertionMarkerBottom = Float.NaN;
            mMatrix.set(Matrix.IDENTITY_MATRIX);
            mMatrixInitialized = false;
            if (mCharacterBoundsArrayBuilder != null) {
                mCharacterBoundsArrayBuilder.reset();
            }
        }
    }

    private CursorAnchorInfo(final Builder builder) {
        mSelectionStart = builder.mSelectionStart;
        mSelectionEnd = builder.mSelectionEnd;
        mComposingTextStart = builder.mComposingTextStart;
        mComposingText = builder.mComposingText;
        mInsertionMarkerFlags = builder.mInsertionMarkerFlags;
        mInsertionMarkerHorizontal = builder.mInsertionMarkerHorizontal;
        mInsertionMarkerTop = builder.mInsertionMarkerTop;
        mInsertionMarkerBaseline = builder.mInsertionMarkerBaseline;
        mInsertionMarkerBottom = builder.mInsertionMarkerBottom;
        mCharacterBoundsArray = builder.mCharacterBoundsArrayBuilder != null ?
                builder.mCharacterBoundsArrayBuilder.build() : null;
        mMatrix = new Matrix(builder.mMatrix);
    }

    /**
     * Returns the index where the selection starts.
     * @return {@code -1} if there is no selection.
     */
    public int getSelectionStart() {
        return mSelectionStart;
    }

    /**
     * Returns the index where the selection ends.
     * @return {@code -1} if there is no selection.
     */
    public int getSelectionEnd() {
        return mSelectionEnd;
    }

    /**
     * Returns the index where the composing text starts.
     * @return {@code -1} if there is no composing text.
     */
    public int getComposingTextStart() {
        return mComposingTextStart;
    }

    /**
     * Returns the entire composing text.
     * @return {@code null} if there is no composition.
     */
    public CharSequence getComposingText() {
        return mComposingText;
    }

    /**
     * Returns the flag of the insertion marker.
     * @return the flag of the insertion marker. {@code 0} if no flag is specified.
     */
    public int getInsertionMarkerFlags() {
        return mInsertionMarkerFlags;
    }

    /**
     * Returns the visibility of the insertion marker.
     * @return {@code true} if the insertion marker is partially or entirely clipped.
     * @removed
     */
    public boolean isInsertionMarkerClipped() {
        return (mInsertionMarkerFlags & FLAG_HAS_VISIBLE_REGION) != 0;
    }

    /**
     * Returns the horizontal start of the insertion marker, in the local coordinates that will
     * be transformed with {@link #getMatrix()} when rendered on the screen.
     * @return x coordinate that is compatible with {@link Layout#getPrimaryHorizontal(int)}.
     * Pay special care to RTL/LTR handling.
     * {@code java.lang.Float.NaN} if not specified.
     * @see Layout#getPrimaryHorizontal(int)
     */
    public float getInsertionMarkerHorizontal() {
        return mInsertionMarkerHorizontal;
    }

    /**
     * Returns the vertical top position of the insertion marker, in the local coordinates that
     * will be transformed with {@link #getMatrix()} when rendered on the screen.
     * @return y coordinate that is compatible with {@link Layout#getLineTop(int)}.
     * {@code java.lang.Float.NaN} if not specified.
     */
    public float getInsertionMarkerTop() {
        return mInsertionMarkerTop;
    }

    /**
     * Returns the vertical baseline position of the insertion marker, in the local coordinates
     * that will be transformed with {@link #getMatrix()} when rendered on the screen.
     * @return y coordinate that is compatible with {@link Layout#getLineBaseline(int)}.
     * {@code java.lang.Float.NaN} if not specified.
     */
    public float getInsertionMarkerBaseline() {
        return mInsertionMarkerBaseline;
    }

    /**
     * Returns the vertical bottom position of the insertion marker, in the local coordinates
     * that will be transformed with {@link #getMatrix()} when rendered on the screen.
     * @return y coordinate that is compatible with {@link Layout#getLineBottom(int)}.
     * {@code java.lang.Float.NaN} if not specified.
     */
    public float getInsertionMarkerBottom() {
        return mInsertionMarkerBottom;
    }

    /**
     * Returns a new instance of {@link RectF} that indicates the location of the character
     * specified with the index.
     * @param index index of the character in a Java chars.
     * @return the character bounds in local coordinates as a new instance of {@link RectF}.
     */
    public RectF getCharacterBounds(final int index) {
        if (mCharacterBoundsArray == null) {
            return null;
        }
        return mCharacterBoundsArray.get(index);
    }

    /**
     * Returns a new instance of {@link RectF} that indicates the location of the character
     * specified with the index.
     * <p>
     * Note that coordinates are not necessarily contiguous or even monotonous, especially when
     * RTL text and LTR text are mixed.
     * </p>
     * @param index index of the character in a Java chars.
     * @return a new instance of {@link RectF} that represents the location of the character in
     * local coordinates. null if the character is invisible or the application did not provide
     * the location. Note that the {@code left} field can be greater than the {@code right} field
     * if the character is in RTL text. Returns {@code null} if no location information is
     * available.
     * @removed
     */
    public RectF getCharacterRect(final int index) {
        return getCharacterBounds(index);
    }

    /**
     * Returns the flags associated with the character bounds specified with the index.
     * @param index index of the character in a Java chars.
     * @return {@code 0} if no flag is specified.
     */
    public int getCharacterBoundsFlags(final int index) {
        if (mCharacterBoundsArray == null) {
            return 0;
        }
        return mCharacterBoundsArray.getFlags(index, 0);
    }

    /**
     * Returns the flags associated with the character rect specified with the index.
     * @param index index of the character in a Java chars.
     * @return {@code 0} if no flag is specified.
     * @removed
     */
    public int getCharacterRectFlags(final int index) {
        return getCharacterBoundsFlags(index);
    }

    /**
     * Returns a new instance of {@link android.graphics.Matrix} that indicates the transformation
     * matrix that is to be applied other positional data in this class.
     * @return a new instance (copy) of the transformation matrix.
     */
    public Matrix getMatrix() {
        return new Matrix(mMatrix);
    }

    /**
     * Used to make this class parcelable.
     */
    public static final Parcelable.Creator<CursorAnchorInfo> CREATOR
            = new Parcelable.Creator<CursorAnchorInfo>() {
        @Override
        public CursorAnchorInfo createFromParcel(Parcel source) {
            return new CursorAnchorInfo(source);
        }

        @Override
        public CursorAnchorInfo[] newArray(int size) {
            return new CursorAnchorInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
